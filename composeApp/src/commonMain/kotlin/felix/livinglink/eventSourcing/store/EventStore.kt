package felix.livinglink.eventSourcing.store

import felix.livinglink.eventSourcing.EventSourcingEvent

interface EventStore {
    suspend fun storeEvents(groupId: String, events: List<EventSourcingEvent>)
    suspend fun getNextExpectedEventId(groupId: String): Long
    suspend fun getEvents(groupId: String): List<EventSourcingEvent>
    suspend fun clearAll()
}

expect class EventDefaultStore() : EventStore