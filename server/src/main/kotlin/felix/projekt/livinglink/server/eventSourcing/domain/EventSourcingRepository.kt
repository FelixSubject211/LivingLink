package felix.projekt.livinglink.server.eventSourcing.domain

import kotlinx.serialization.json.JsonElement

interface EventSourcingRepository {
    suspend fun appendEvent(
        groupId: String,
        topic: String,
        createdBy: String,
        payload: JsonElement,
        expectedLastEventId: Long
    ): EventSourcingEvent?

    suspend fun fetchEvents(
        groupId: String,
        topic: String,
        lastKnownEventId: Long,
        limit: Int
    ): List<EventSourcingEvent>

    suspend fun totalEvents(
        groupId: String,
        topic: String
    ): Long

    suspend fun deleteGroupEvents(groupId: String)

    suspend fun anonymizeUserEvents(
        groupId: String,
        userId: String,
        anonymizedUserId: String
    )

    fun close()
}
