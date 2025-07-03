package felix.livinglink.eventSourcing.store

import felix.livinglink.eventSourcing.EventSourcingEvent

interface EventStore {
    suspend fun storeEvents(groupId: String, events: List<EventSourcingEvent<*>>)
    suspend fun getNextExpectedEventId(groupId: String): Long
    suspend fun getEventsSince(groupId: String, eventIdExclusive: Long): List<EventSourcingEvent<*>>
    suspend fun anonymizeUserIdsIndividually(groupId: String, originalUserId: String)
    suspend fun clearAll()
}

expect class EventDefaultStore() : EventStore