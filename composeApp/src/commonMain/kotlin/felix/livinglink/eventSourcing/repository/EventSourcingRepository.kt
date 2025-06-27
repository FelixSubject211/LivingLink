package felix.livinglink.eventSourcing.repository

import felix.livinglink.common.model.LivingLinkResult
import felix.livinglink.common.model.RepositoryState
import felix.livinglink.common.network.NetworkError
import felix.livinglink.event.eventbus.EventBus
import felix.livinglink.eventSourcing.AppendEventSourcingEventRequest
import felix.livinglink.eventSourcing.EventSourcingEvent
import felix.livinglink.eventSourcing.network.EventSourcingNetworkDataSource
import felix.livinglink.eventSourcing.store.AggregateStore
import felix.livinglink.eventSourcing.store.EventStore
import io.ktor.util.collections.ConcurrentMap
import kotlinx.coroutines.CoroutineScope
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
        isEmpty: (A) -> Boolean,
        serializer: KSerializer<A>
    ): Flow<RepositoryState<A, Nothing>>

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
    private val aggregateFlows = ConcurrentMap<String, MutableStateFlow<Any?>>()
    private val loadingFlows = ConcurrentMap<String, MutableStateFlow<Boolean?>>()
    private val deferredSyncCallbacks = mutableSetOf<PostReductionAction>()
    private val channelCollectors = ConcurrentMap<String, kotlinx.coroutines.Job>()

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
                appendEvents(groupId = groupId, newEvents = result.data.events)
            }

            is LivingLinkResult.Error<*> -> {}
        }
    }

    private suspend fun appendEvents(
        groupId: String,
        newEvents: List<EventSourcingEvent>
    ) = mutex.withLock {
        val expectedNextId = eventStore.getNextExpectedEventId(groupId)

        val firstIncomingId = newEvents.first().eventId
        val offset = firstIncomingId - expectedNextId

        if (offset > 0) {
            println("Expected eventId=$expectedNextId, got $firstIncomingId")
            return@withLock
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
        isEmpty: (A) -> Boolean,
        serializer: KSerializer<A>
    ): Flow<RepositoryState<A, Nothing>> {
        val cacheKey = "$groupId:${type.qualifiedName}:$aggregationKey"

        @Suppress("UNCHECKED_CAST")
        val aggregateFlow = aggregateFlows.getOrPut(cacheKey) {
            MutableStateFlow(value = null)
        } as MutableStateFlow<A?>

        val loadingFlow = loadingFlows.getOrPut(cacheKey) {
            MutableStateFlow(value = null)
        }

        val postReductionAction = PostReductionAction({
            if (loadingFlow.value != false) {
                loadingFlow.value = false
            }
        })

        scope.launch {
            mutex.withLock {
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
                    val foldedAggregate = relevantEvents.fold(initial) { acc, event ->
                        acc.applyEvent(event)
                    }
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
                                    postReductionAction.setReducingState(true)
                                    val currentOrInitial = aggregateFlow.value ?: initial
                                    val updated = eventBatch.fold(currentOrInitial) { acc, event ->
                                        acc.applyEvent(event)
                                    }
                                    aggregateFlow.value = updated
                                    aggregateStore.store(cacheKey, serializer, updated)
                                    postReductionAction.run()
                                    postReductionAction.setReducingState(false)
                                }
                            }
                    }.also { job -> channelCollectors[cacheKey] = job }
                }
            }
        }

        return combine(aggregateFlow, loadingFlow) { aggregate, loading ->
            if (aggregate == null) {
                null
            } else if (loading == true) {
                RepositoryState.Loading(aggregate)
            } else {
                if (isEmpty(aggregate)) {
                    RepositoryState.Empty
                } else {
                    RepositoryState.Data(aggregate)
                }
            }
        }.mapNotNull { it }
    }

    override suspend fun addEvent(
        groupId: String,
        payload: EventSourcingEvent.Payload
    ): LivingLinkResult<Unit, NetworkError> {
        return when (val result = eventSourcingNetworkDataSource.appendEvent(
            AppendEventSourcingEventRequest(groupId, payload)
        )) {
            is LivingLinkResult.Success -> {
                appendEvents(groupId, listOf(result.data.event))
                LivingLinkResult.Success(Unit)
            }
            is LivingLinkResult.Error -> result
        }
    }

    private class PostReductionAction(
        private val action: () -> Unit,
        private var isReducing: Boolean = false
    ) {
        fun runIfNotReducing() {
            if(!isReducing) {
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