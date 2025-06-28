package felix.livinglink.eventSourcing.repository

import felix.livinglink.common.model.LivingLinkError
import felix.livinglink.common.model.LivingLinkResult
import felix.livinglink.common.model.RepositoryState
import felix.livinglink.common.model.UnknownError
import felix.livinglink.common.network.NetworkError
import felix.livinglink.event.eventbus.EventBus
import felix.livinglink.eventSourcing.AppendEventSourcingEventRequest
import felix.livinglink.eventSourcing.EventSourcingEvent
import felix.livinglink.eventSourcing.network.EventSourcingNetworkDataSource
import felix.livinglink.eventSourcing.store.AggregateStore
import felix.livinglink.eventSourcing.store.EventStore
import io.ktor.util.collections.ConcurrentMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

interface EventSourcingRepository {
    fun <T : EventSourcingEvent.Payload, A : Aggregate<A>> aggregateState(
        groupId: String,
        aggregationKey: String,
        type: KClass<T>,
        initial: A,
        serializer: KSerializer<A>
    ): Flow<RepositoryState<A, LivingLinkError>>

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
    private val scope: CoroutineScope
) : EventSourcingRepository {

    private val mutex = Mutex()
    private val eventChannels = ConcurrentMap<String, Channel<List<EventSourcingEvent>>>()
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
        eventChannels.clear()
        aggregateFlows.clear()
        loadingFlows.clear()
        errorFlows.clear()
        deferredSyncCallbacks.clear()
        channelCollectors.clear()
    }

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
        newEvents: List<EventSourcingEvent>
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

        val eventChannel = eventChannels.getOrPut(groupId) {
            Channel(capacity = Channel.UNLIMITED, onBufferOverflow = BufferOverflow.SUSPEND)
        }

        eventChannel.send(newOnly)
    }

    override fun <T : EventSourcingEvent.Payload, A : Aggregate<A>> aggregateState(
        groupId: String,
        aggregationKey: String,
        type: KClass<T>,
        initial: A,
        serializer: KSerializer<A>
    ): Flow<RepositoryState<A, LivingLinkError>> {
        val cacheKey = CacheKey(
            groupId = groupId,
            qualifiedTypeName = type.qualifiedName!!,
            aggregationKey = aggregationKey
        )

        @Suppress("UNCHECKED_CAST")
        val aggregateFlow = aggregateFlows.getOrPut(cacheKey) {
            MutableStateFlow(value = null)
        } as MutableStateFlow<A?>

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
                        type = type,
                        initial = initial,
                        serializer = serializer,
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

    private suspend fun <T : EventSourcingEvent.Payload, A : Aggregate<A>> loadAndObserveAggregate(
        groupId: String,
        cacheKey: CacheKey,
        type: KClass<T>,
        initial: A,
        serializer: KSerializer<A>,
        aggregateFlow: MutableStateFlow<A?>,
        loadingFlow: MutableStateFlow<Boolean?>,
        postReductionAction: PostReductionAction
    ) {
        val aggregate = aggregateStore.get(cacheKey, serializer)

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
            val relevantEvents = allEvents.filter { type.isInstance(it.payload) }
            val foldedAggregate = reduceEvents(initial, relevantEvents)
            aggregateFlow.value = foldedAggregate
            aggregateStore.store(cacheKey, serializer, foldedAggregate)
        }

        val liveEventChannel = eventChannels.getOrPut(groupId) {
            Channel(Channel.BUFFERED, onBufferOverflow = BufferOverflow.SUSPEND)
        }

        channelCollectors.getOrPut(cacheKey) {
            scope.launch {
                liveEventChannel.receiveAsFlow()
                    .filter { type.isInstance(it.firstOrNull()?.payload) }
                    .collect { eventBatch ->
                        mutex.withLock {
                            try {
                                postReductionAction.setReducingState(true)
                                val currentOrInitial = aggregateFlow.value ?: initial
                                val updated = reduceEvents(currentOrInitial, eventBatch)
                                aggregateFlow.value = updated
                                aggregateStore.store(cacheKey, serializer, updated)
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

    private fun <A : Aggregate<A>> reduceEvents(
        initial: A,
        eventBatch: List<EventSourcingEvent>
    ): A {
        var aggregate = initial
        for (event in eventBatch) {
            val expectedId = (aggregate.getLastEventId() ?: -1) + 1
            if (event.eventId != expectedId) {
                throw IllegalStateException(
                    "expectedId = $expectedId, eventId = ${event.eventId}"
                )
            }
            aggregate = aggregate.applyEvent(event)
        }
        return aggregate
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