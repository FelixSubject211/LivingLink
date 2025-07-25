package felix.livinglink.eventSourcing.store

import felix.livinglink.common.store.createStore
import felix.livinglink.eventSourcing.EventSourcingEvent
import io.github.xxfast.kstore.KStore

actual class EventDefaultStore : EventStore {
    private val eventStore: KStore<Map<String, List<EventSourcingEvent<*>>>> = createStore(
        path = "events",
        defaultValue = emptyMap()
    )

    private val nextExpectedEventIdStore: KStore<Map<String, Long>> = createStore(
        path = "events-next-ids",
        defaultValue = emptyMap()
    )

    override suspend fun storeEvents(groupId: String, events: List<EventSourcingEvent<*>>) {
        eventStore.update { current ->
            val map = current ?: emptyMap()
            val existing = map[groupId].orEmpty()
            val updated = existing + events
            map + (groupId to updated)
        }
        nextExpectedEventIdStore.update { current ->
            val map = current ?: emptyMap()
            map + (groupId to events.last().eventId + 1)
        }
    }

    override suspend fun getNextExpectedEventId(groupId: String): Long {
        return nextExpectedEventIdStore.get()?.get(groupId) ?: 0
    }

    override suspend fun getEventsSince(
        groupId: String,
        eventIdExclusive: Long
    ): List<EventSourcingEvent<*>> {
        val all = eventStore.get()?.get(groupId).orEmpty()
        return all.filter { it.eventId > eventIdExclusive }
    }

    override suspend fun anonymizeUserIdsIndividually(groupId: String, originalUserId: String) {
        eventStore.update { current ->
            val map = current ?: return@update emptyMap()

            val existingEvents = map[groupId].orEmpty()
            val updatedEvents = existingEvents.map { event ->
                if (event.userId == originalUserId) {
                    event.copy(userId = null)
                } else {
                    event
                }
            }

            map + (groupId to updatedEvents)
        }
    }

    override suspend fun clearAll() {
        eventStore.update { emptyMap() }
        nextExpectedEventIdStore.update { emptyMap() }
    }
}