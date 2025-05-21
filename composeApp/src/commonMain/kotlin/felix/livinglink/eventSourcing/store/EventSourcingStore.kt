package felix.livinglink.eventSourcing.store

import felix.livinglink.common.model.RepositoryState
import felix.livinglink.common.store.createStore
import felix.livinglink.eventSourcing.EventSourcingEvent
import felix.livinglink.json
import io.github.xxfast.kstore.KStore
import io.ktor.util.collections.ConcurrentMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

interface EventSourcingStore {

    fun eventsForGroup(groupId: String): Flow<List<EventSourcingEvent>>

    fun <T : EventSourcingEvent.Payload, A> aggregateState(
        groupId: String,
        aggregationKey: String,
        type: KClass<T>,
        initial: A,
        reduce: (A, EventSourcingEvent) -> A,
        isEmpty: (A) -> Boolean,
        serializer: KSerializer<A>
    ): Flow<RepositoryState<A, Nothing>>

    suspend fun clearAll()

    suspend fun appendEvents(groupId: String, newEvents: List<EventSourcingEvent>)

    suspend fun getNextExpectedEventId(groupId: String): Long
}

class EventSourcingDefaultStore(
    private val scope: CoroutineScope
) : EventSourcingStore {
    private val eventStore: KStore<Map<String, List<EventSourcingEvent>>> = createStore(
        path = "event-sourcing",
        defaultValue = emptyMap()
    )

    private val nextExpectedEventIdStore: KStore<Map<String, Long>> = createStore(
        path = "event-sourcing-next-event-ids",
        defaultValue = emptyMap()
    )

    private val aggregateStore: KStore<Map<String, String>> = createStore(
        path = "event-sourcing-aggregates",
        defaultValue = emptyMap()
    )

    private val mutex = Mutex()

    private val eventChannels = ConcurrentMap<String, Channel<List<EventSourcingEvent>>>()
    private val aggregateFlows = ConcurrentMap<String, MutableStateFlow<Any>>()
    private val loadingFlows = ConcurrentMap<String, MutableStateFlow<Boolean>>()
    private val timeoutFlows = ConcurrentMap<String, MutableStateFlow<Boolean>>()
    private val initializedAggregates = mutableSetOf<String>()

    override fun eventsForGroup(groupId: String): Flow<List<EventSourcingEvent>> {
        return eventStore.updates.map { it?.get(groupId).orEmpty() }
    }

    override fun <T : EventSourcingEvent.Payload, A> aggregateState(
        groupId: String,
        aggregationKey: String,
        type: KClass<T>,
        initial: A,
        reduce: (A, EventSourcingEvent) -> A,
        isEmpty: (A) -> Boolean,
        serializer: KSerializer<A>
    ): Flow<RepositoryState<A, Nothing>> {
        val cacheKey = "$groupId:${type.qualifiedName}:$aggregationKey"

        @Suppress("UNCHECKED_CAST")
        val aggregateStateFlow = aggregateFlows.getOrPut(cacheKey) {
            MutableStateFlow(initial as Any)
        } as MutableStateFlow<A>

        val isLoadingFlow = loadingFlows.getOrPut(cacheKey) {
            MutableStateFlow(false)
        }

        val timeoutFlow = timeoutFlows.getOrPut(cacheKey) { MutableStateFlow(false) }

        scope.launch {
            mutex.withLock {
                val storedJsonString = aggregateStore.get()?.get(cacheKey)
                val cachedAggregate: A? = runCatching {
                    storedJsonString?.let { json.decodeFromString(serializer, it) }
                }.getOrNull()

                if (cachedAggregate != null) {
                    aggregateStateFlow.value = cachedAggregate
                } else {
                    isLoadingFlow.value = true

                    val allEvents = eventStore.get()?.get(groupId).orEmpty()
                    val relevantEvents = allEvents.filter { type.isInstance(it.payload) }
                    val foldedAggregate = relevantEvents.fold(initial, reduce)
                    aggregateStateFlow.value = foldedAggregate

                    val serializedAggregate = json.encodeToString(serializer, foldedAggregate)
                    aggregateStore.update { current ->
                        (current ?: emptyMap()) + (cacheKey to serializedAggregate)
                    }
                }

                val receivedUpdate = MutableStateFlow(false)

                scope.launch {
                    delay(5000)
                    if (!receivedUpdate.value) {
                        timeoutFlow.value = true
                    }
                }

                val liveEventChannel = eventChannels.getOrPut(groupId) {
                    Channel(Channel.BUFFERED, onBufferOverflow = BufferOverflow.SUSPEND)
                }
                scope.launch {
                    liveEventChannel.receiveAsFlow()
                        .filter { type.isInstance(it.firstOrNull()?.payload) }
                        .collect { eventBatch ->
                            mutex.withLock {
                                val updated = eventBatch.fold(aggregateStateFlow.value, reduce)
                                aggregateStateFlow.value = updated
                                val serialized = json.encodeToString(serializer, updated)
                                aggregateStore.update { it.orEmpty() + (cacheKey to serialized) }
                            }
                        }
                }
            }
        }

        return combine(
            aggregateStateFlow, isLoadingFlow, timeoutFlow
        ) { aggregate, isLoading, timeout ->
            when {
                isLoading && !timeout -> RepositoryState.Loading(aggregate)
                isEmpty(aggregate) -> RepositoryState.Empty
                else -> RepositoryState.Data(aggregate)
            }
        }
    }

    override suspend fun clearAll() = mutex.withLock {
        eventStore.update { emptyMap() }
        nextExpectedEventIdStore.update { emptyMap() }
        aggregateStore.update { emptyMap() }
        aggregateFlows.clear()
        initializedAggregates.clear()
        loadingFlows.clear()
        eventChannels.clear()
        timeoutFlows.clear()
    }

    override suspend fun appendEvents(
        groupId: String,
        newEvents: List<EventSourcingEvent>
    ) = mutex.withLock {
        val expectedNextId = getNextExpectedEventId(groupId)
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

        eventStore.update { current ->
            val map = current ?: emptyMap()
            val existing = map[groupId].orEmpty()
            val updated = existing + newOnly
            map + (groupId to updated)
        }

        newOnly.lastOrNull()?.let { lastEvent ->
            nextExpectedEventIdStore.update { current ->
                val map = current ?: emptyMap()
                map + (groupId to lastEvent.eventId + 1)
            }
        }

        val eventChannel = eventChannels.getOrPut(groupId) {
            Channel(capacity = Channel.UNLIMITED, onBufferOverflow = BufferOverflow.SUSPEND)
        }

        eventChannel.send(newOnly)
    }

    override suspend fun getNextExpectedEventId(groupId: String): Long {
        return nextExpectedEventIdStore.get()?.get(groupId) ?: 0
    }
}