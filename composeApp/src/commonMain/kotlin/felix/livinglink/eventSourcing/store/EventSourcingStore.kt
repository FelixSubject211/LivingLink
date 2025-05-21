package felix.livinglink.eventSourcing.store

import felix.livinglink.common.model.RepositoryState
import felix.livinglink.common.store.createStore
import felix.livinglink.eventSourcing.EventSourcingEvent
import io.github.xxfast.kstore.KStore
import io.ktor.util.collections.ConcurrentMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.reflect.KClass

interface EventSourcingStore {

    fun eventsForGroup(groupId: String): Flow<List<EventSourcingEvent>>

    fun <T : EventSourcingEvent.Payload, A> aggregateState(
        groupId: String,
        type: KClass<T>,
        initial: A,
        reduce: (A, EventSourcingEvent) -> A,
        isEmpty: (A) -> Boolean,
        aggregationKey: String
    ): Flow<RepositoryState<A, Nothing>>

    suspend fun clearAll()

    suspend fun appendEvents(groupId: String, newEvents: List<EventSourcingEvent>)

    suspend fun getNextExpectedEventId(groupId: String): Long
}

class EventSourcingDefaultStore(
    private val scope: CoroutineScope
) : EventSourcingStore {
    private val store: KStore<Map<String, List<EventSourcingEvent>>> = createStore(
        path = "event-sourcing",
        defaultValue = emptyMap()
    )

    private val mutex = Mutex()

    private val eventChannels = ConcurrentMap<String, Channel<EventSourcingEvent>>()
    private val aggregateFlows = ConcurrentMap<String, MutableStateFlow<Any>>()
    private val initializedAggregates = mutableSetOf<String>()
    private val loadingFlows = ConcurrentMap<String, MutableStateFlow<Boolean>>()

    override fun eventsForGroup(groupId: String): Flow<List<EventSourcingEvent>> {
        return store.updates.map { it?.get(groupId).orEmpty() }
    }

    override fun <T : EventSourcingEvent.Payload, A> aggregateState(
        groupId: String,
        type: KClass<T>,
        initial: A,
        reduce: (A, EventSourcingEvent) -> A,
        isEmpty: (A) -> Boolean,
        aggregationKey: String
    ): Flow<RepositoryState<A, Nothing>> {
        val cacheKey = "$groupId:${type.qualifiedName}:$aggregationKey"

        @Suppress("UNCHECKED_CAST")
        val stateFlow = aggregateFlows.getOrPut(cacheKey) {
            MutableStateFlow(initial as Any)
        } as MutableStateFlow<A>

        val isLoadingFlow = loadingFlows.getOrPut(cacheKey) {
            MutableStateFlow(true)
        }

        scope.launch {
            mutex.withLock {
                if (cacheKey !in initializedAggregates) {
                    initializedAggregates += cacheKey
                    val events = store.get()?.get(groupId).orEmpty()
                    val relevant = events.filter { type.isInstance(it.payload) }
                    val result = relevant.fold(initial, reduce)
                    stateFlow.value = result
                    isLoadingFlow.value = false

                    val channel = eventChannels.getOrPut(groupId) {
                        Channel(Channel.BUFFERED, onBufferOverflow = BufferOverflow.SUSPEND)
                    }

                    scope.launch {
                        channel.receiveAsFlow()
                            .filter { type.isInstance(it.payload) }
                            .collect { event ->
                                stateFlow.value = reduce(stateFlow.value, event)
                            }
                    }
                }
            }
        }

        return stateFlow
            .asStateFlow()
            .combine(isLoadingFlow) { current, loading ->
                when {
                    loading -> RepositoryState.Loading(null)
                    isEmpty(current) -> RepositoryState.Empty
                    else -> RepositoryState.Data(current)
                }
            }
    }

    override suspend fun clearAll() {
        store.update { emptyMap() }
        aggregateFlows.clear()
        initializedAggregates.clear()
        loadingFlows.clear()
        eventChannels.clear()
    }

    override suspend fun appendEvents(
        groupId: String,
        newEvents: List<EventSourcingEvent>
    ) = mutex.withLock {
        val expectedNextId = getNextExpectedEventId(groupId)

        val sortedIncoming = newEvents.sortedBy { it.eventId }

        val firstId = sortedIncoming.first().eventId
        if (firstId != expectedNextId) {
            println("Expected eventId=$expectedNextId, got $firstId")
            return@withLock
        }

        for (i in sortedIncoming.indices) {
            val expectedId = expectedNextId + i
            val actualId = sortedIncoming[i].eventId
            if (actualId != expectedId) {
                println("Event sequence mismatch at index $i: expected $expectedId, got $actualId")
                return@withLock
            }
        }

        store.update { current ->
            val map = current ?: emptyMap()
            val existing = map[groupId].orEmpty()
            val updated = (existing + sortedIncoming).sortedBy { it.eventId }
            map + (groupId to updated)
        }

        val channel = eventChannels.getOrPut(groupId) {
            Channel(capacity = Channel.UNLIMITED, onBufferOverflow = BufferOverflow.SUSPEND)
        }

        sortedIncoming.forEach { channel.send(it) }
    }

    override suspend fun getNextExpectedEventId(groupId: String): Long {
        val events = store.get()?.get(groupId) ?: return 0
        return events.maxOfOrNull { it.eventId }?.plus(1) ?: 0
    }
}