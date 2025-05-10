package felix.livinglink.eventSourcing.store

import felix.livinglink.common.store.createStore
import felix.livinglink.eventSourcing.EventSourcingEvent
import io.github.xxfast.kstore.KStore
import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.map

interface EventSourcingStore {
    fun all(groupId: String): Flow<List<EventSourcingEvent>>
    fun <T : EventSourcingEvent.Payload> ofType(
        groupId: String,
        type: KClass<T>
    ): Flow<List<EventSourcingEvent>>
    suspend fun clear()
    suspend fun merge(groupId: String, newEvents: List<EventSourcingEvent>)
}

class EventSourcingDefaultStore : EventSourcingStore {
    private val store: KStore<Map<String, List<EventSourcingEvent>>> = createStore(
        path = "event-sourcing",
        defaultValue = emptyMap()
    )

    override fun all(groupId: String): Flow<List<EventSourcingEvent>> {
        return store.updates.map { it?.get(groupId).orEmpty() }
    }

    override fun <T : EventSourcingEvent.Payload> ofType(
        groupId: String,
        type: KClass<T>
    ): Flow<List<EventSourcingEvent>> {
        return all(groupId).map { list ->
            list.filter { type.isInstance(it.payload) }
        }
    }

    override suspend fun clear() {
        store.update { emptyMap() }
    }

    override suspend fun merge(groupId: String, newEvents: List<EventSourcingEvent>) {
        store.update { current ->

            val existing = current?.get(groupId).orEmpty()
            val knownIds = existing.associateBy { it.eventId }

            val merged = (existing + newEvents)
                .filterNot { knownIds.containsKey(it.eventId) }
                .let { existing + it }
                .sortedBy { it.eventId }

            current?.plus((groupId to merged))
        }
    }
}