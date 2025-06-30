package felix.livinglink.eventSourcing.repository

import felix.livinglink.common.model.LivingLinkError
import felix.livinglink.common.model.LivingLinkResult
import felix.livinglink.common.model.RepositoryState
import felix.livinglink.common.model.UnknownError
import felix.livinglink.common.network.NetworkError
import felix.livinglink.event.eventbus.EventBus
import felix.livinglink.eventSourcing.AppendEventSourcingEventRequest
import felix.livinglink.eventSourcing.EventSourcingEvent
import felix.livinglink.eventSourcing.UserAnonymized
import felix.livinglink.eventSourcing.network.EventSourcingNetworkDataSource
import felix.livinglink.eventSourcing.store.AggregateStore
import felix.livinglink.eventSourcing.store.EventStore
import io.ktor.util.collections.ConcurrentMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.reflect.KClass
import kotlin.uuid.ExperimentalUuidApi

interface EventSourcingRepository {
    fun <PAYLOAD : EventSourcingEvent.Payload, AGGREGATE : Aggregate<AGGREGATE, PAYLOAD>> aggregateState(
        groupId: String,
        aggregationKey: String,
        payloadType: KClass<PAYLOAD>,
        initial: AGGREGATE
    ): Flow<RepositoryState<AGGREGATE, LivingLinkError>>

    suspend fun addEvent(
        groupId: String,
        payload: EventSourcingEvent.Payload
    ): LivingLinkResult<Unit, NetworkError>
}

class EventSourcingDefaultRepository(
    private val eventSourcingNetworkDataSource: EventSourcingNetworkDataSource,
    private val eventStore: EventStore,
    private val aggregateStore: AggregateStore,
    private val eventBus: EventBus,
    private val scope: CoroutineScope,
) : EventSourcingRepository {
    private val mutex = Mutex()
    private val eventFlows = ConcurrentMap<String, MutableSharedFlow<List<EventSourcingEvent<*>>>>()
    private val aggregateFlows = ConcurrentMap<CacheKey, MutableStateFlow<Any?>>()
    private val loadingFlows = ConcurrentMap<CacheKey, MutableStateFlow<Boolean?>>()
    private val errorFlows = ConcurrentMap<CacheKey, MutableStateFlow<LivingLinkError?>>()
    private val deferredSyncCallbacks = mutableSetOf<PostReductionAction>()
    private val channelCollectors = ConcurrentMap<CacheKey, Job>()

    init {
        observeEventBus()
    }

    private fun observeEventBus() {
        scope.launch {
            eventBus.events.collect { event ->
                when (event) {
                    is EventBus.Event.GroupStateUpdated -> {
                        handleGroupStateUpdate(event.groupId, event.latestEventId)
                    }

                    is EventBus.Event.ClearAll -> {
                        clearAll()
                    }

                    else -> {}
                }
            }
        }
    }

    private suspend fun clearAll() = mutex.withLock {
        eventStore.clearAll()
        aggregateStore.clearAll()
        eventFlows.clear()
        aggregateFlows.clear()
        loadingFlows.clear()
        errorFlows.clear()
        deferredSyncCallbacks.clear()
        channelCollectors.clear()
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun handleGroupStateUpdate(groupId: String, latestRemoteId: Long?) {
        val expectedLocalId = eventStore.getNextExpectedEventId(groupId)

        if (latestRemoteId == null || expectedLocalId > latestRemoteId) {
            deferredSyncCallbacks.forEach { it.runIfNotReducing() }
            deferredSyncCallbacks.clear()
            return
        }

        val syncStartExclusiveId = expectedLocalId - 1

        when (val result = eventSourcingNetworkDataSource.getEvents(
            groupId = groupId,
            sinceEventIdExclusive = syncStartExclusiveId
        )) {
            is LivingLinkResult.Success -> {
                try {
                    storeEventsAndSendToChannel(groupId = groupId, newEvents = result.data.events)

                    val relevantEvents = result.data.events.filterByPayloadType(
                        UserAnonymized::class
                    )

                    @Suppress("UNCHECKED_CAST")
                    relevantEvents.forEach { userAnonymizedEvent ->
                        val originalUserId = userAnonymizedEvent.payload.originalUserId

                        eventStore.anonymizeUserIdsIndividually(
                            groupId = groupId,
                            originalUserId = originalUserId
                        )

                        aggregateFlows
                            .filter { it.key.groupId == groupId }
                            .forEach { (key, flow) ->
                                val current =
                                    flow.value as Aggregate<Any, EventSourcingEvent.Payload>
                                val anonymized = current.anonymizeUser(originalUserId)
                                if (anonymized != current) {
                                    flow.value = anonymized
                                    val serializer = current.serializer()
                                    aggregateStore.store(key, serializer, anonymized)
                                }
                            }
                    }

                } catch (e: Exception) {
                    println(e)
                }
            }

            is LivingLinkResult.Error<*> -> {
                sendErrorToAllFromGroup(
                    groupId = groupId,
                    error = result.error as NetworkError
                )
            }
        }
    }

    private suspend fun sendErrorToAllFromGroup(groupId: String, error: LivingLinkError) {
        mutex.withLock {
            val errorFlows = errorFlows.filter { it.key.groupId == groupId }

            errorFlows.forEach {
                it.value.value = error
            }
        }
    }

    private suspend fun storeEventsAndSendToChannel(
        groupId: String,
        newEvents: List<EventSourcingEvent<*>>
    ) = mutex.withLock {
        val expectedNextId = eventStore.getNextExpectedEventId(groupId)

        val firstIncomingId = newEvents.first().eventId
        val offset = firstIncomingId - expectedNextId

        if (offset > 0) {
            throw IllegalStateException("Expected eventId=$expectedNextId, got $firstIncomingId")
        }

        val newOnly = newEvents.drop(-offset.toInt())

        if (newOnly.isEmpty()) {
            return@withLock
        }

        eventStore.storeEvents(groupId = groupId, events = newOnly)

        val flow = eventFlows.getOrPut(groupId) {
            MutableSharedFlow(
                replay = 0,
                extraBufferCapacity = Int.MAX_VALUE,
                onBufferOverflow = BufferOverflow.SUSPEND
            )
        }
        flow.emit(newOnly)
    }

    override fun <PAYLOAD : EventSourcingEvent.Payload, AGGREGATE : Aggregate<AGGREGATE, PAYLOAD>> aggregateState(
        groupId: String,
        aggregationKey: String,
        payloadType: KClass<PAYLOAD>,
        initial: AGGREGATE
    ): Flow<RepositoryState<AGGREGATE, LivingLinkError>> {
        val cacheKey = CacheKey(
            groupId = groupId,
            qualifiedTypeName = payloadType.qualifiedName!!,
            aggregationKey = aggregationKey
        )

        @Suppress("UNCHECKED_CAST")
        val aggregateFlow = aggregateFlows.getOrPut(cacheKey) {
            MutableStateFlow(value = null)
        } as MutableStateFlow<AGGREGATE?>

        val loadingFlow = loadingFlows.getOrPut(cacheKey) {
            MutableStateFlow(value = null)
        }

        val errorFlow = errorFlows.getOrPut(cacheKey) {
            MutableStateFlow(value = null)
        }

        val postReductionAction = PostReductionAction({
            if (loadingFlow.value != false) {
                loadingFlow.value = false
            }
        })

        scope.launch {
            mutex.withLock {
                try {
                    loadAndObserveAggregate(
                        groupId = groupId,
                        cacheKey = cacheKey,
                        payloadType = payloadType,
                        initial = initial,
                        aggregateFlow = aggregateFlow,
                        loadingFlow = loadingFlow,
                        postReductionAction = postReductionAction
                    )
                } catch (e: Exception) {
                    handleInconsistency(
                        error = e,
                        cacheKey = cacheKey
                    )
                }
            }
        }

        return combine(aggregateFlow, loadingFlow, errorFlow) { aggregate, loading, error ->
            if (error != null) {
                RepositoryState.Error(error)
            } else if (aggregate == null) {
                null
            } else if (loading == true) {
                RepositoryState.Loading(aggregate)
            } else {
                if (aggregate.isEmpty()) {
                    RepositoryState.Empty
                } else {
                    RepositoryState.Data(aggregate)
                }
            }
        }.mapNotNull { it }.also { errorFlow.value = null }
    }

    private suspend fun <PAYLOAD : EventSourcingEvent.Payload, AGGREGATE : Aggregate<AGGREGATE, PAYLOAD>> loadAndObserveAggregate(
        groupId: String,
        cacheKey: CacheKey,
        payloadType: KClass<PAYLOAD>,
        initial: AGGREGATE,
        aggregateFlow: MutableStateFlow<AGGREGATE?>,
        loadingFlow: MutableStateFlow<Boolean?>,
        postReductionAction: PostReductionAction
    ) {
        val aggregate = aggregateStore.get(cacheKey, initial.serializer())

        if (aggregate != null) {
            aggregateFlow.value = aggregate
            loadingFlow.value = false
        } else {
            val allEvents = eventStore.getEvents(groupId)
            if (allEvents.isEmpty()) {
                loadingFlow.value = true
                deferredSyncCallbacks.add(postReductionAction)
            } else {
                loadingFlow.value = false
            }

            val relevantEvents = allEvents.filterByPayloadType(payloadType)

            val foldedAggregate = relevantEvents.fold(initial) { acc, event ->
                acc.applyEvent(event)
            }
            aggregateFlow.value = foldedAggregate
            aggregateStore.store(cacheKey, foldedAggregate.serializer(), foldedAggregate)
        }

        val liveEventFlow = eventFlows.getOrPut(groupId) {
            MutableSharedFlow(
                replay = 0,
                extraBufferCapacity = Int.MAX_VALUE,
                onBufferOverflow = BufferOverflow.SUSPEND
            )
        }

        channelCollectors.getOrPut(cacheKey) {
            scope.launch {
                liveEventFlow
                    .collect { eventBatch ->
                        mutex.withLock {
                            try {
                                postReductionAction.setReducingState(true)
                                val relevantEvents = eventBatch.filterByPayloadType(payloadType)
                                val currentOrInitial = aggregateFlow.value ?: initial
                                val updated = relevantEvents.fold(currentOrInitial) { acc, event ->
                                    acc.applyEvent(event)
                                }
                                aggregateFlow.value = updated
                                aggregateStore.store(cacheKey, updated.serializer(), updated)
                                postReductionAction.run()
                                postReductionAction.setReducingState(false)
                            } catch (e: Exception) {
                                handleInconsistency(
                                    error = e,
                                    cacheKey = cacheKey
                                )
                            }
                        }
                    }
            }.also { job -> channelCollectors[cacheKey] = job }
        }
    }

    private fun handleInconsistency(
        error: Exception,
        cacheKey: CacheKey
    ) {
        val errorFlow = errorFlows.getOrPut(cacheKey) {
            MutableStateFlow(value = null)
        }
        errorFlow.value = UnknownError(error)
    }

    override suspend fun addEvent(
        groupId: String,
        payload: EventSourcingEvent.Payload
    ): LivingLinkResult<Unit, NetworkError> {
        return when (val result = eventSourcingNetworkDataSource.appendEvent(
            AppendEventSourcingEventRequest(groupId, payload)
        )) {
            is LivingLinkResult.Success -> {
                try {
                    storeEventsAndSendToChannel(groupId, listOf(result.data.event))
                } catch (e: Exception) {
                    println(e)
                }
                LivingLinkResult.Success(Unit)
            }

            is LivingLinkResult.Error -> {
                result
            }
        }
    }

    private fun <T : EventSourcingEvent.Payload> List<EventSourcingEvent<*>>.filterByPayloadType(
        type: KClass<T>
    ): List<EventSourcingEvent<T>> {
        return this.mapNotNull { event ->
            if (type.isInstance(event.payload)) {
                @Suppress("UNCHECKED_CAST")
                event as EventSourcingEvent<T>
            } else {
                null
            }
        }
    }

    private class PostReductionAction(
        private val action: () -> Unit,
        private var isReducing: Boolean = false
    ) {
        fun runIfNotReducing() {
            if (!isReducing) {
                action()
            }
        }

        fun setReducingState(isReducing: Boolean) {
            this.isReducing = isReducing
        }

        fun run() {
            action()
        }
    }
}