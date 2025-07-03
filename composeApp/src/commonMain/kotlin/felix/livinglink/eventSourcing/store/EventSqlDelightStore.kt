package felix.livinglink.eventSourcing.store

import app.cash.sqldelight.db.SqlDriver
import felix.livinglink.db.AppDatabase
import felix.livinglink.eventSourcing.EventSourcingEvent
import felix.livinglink.json
import kotlinx.datetime.Instant
import kotlinx.serialization.PolymorphicSerializer

class EventSqlDelightStore(
    driver: SqlDriver
) : EventStore {

    private val queries = AppDatabase(driver).eventSourcingQueries
    private val payloadSerializer = PolymorphicSerializer(EventSourcingEvent.Payload::class)

    override suspend fun storeEvents(groupId: String, events: List<EventSourcingEvent<*>>) {
        queries.transaction {
            events.forEach { event ->
                queries.insertEvent(
                    event_id = event.eventId,
                    user_id = event.userId,
                    group_id = event.groupId,
                    created_at = event.createdAt.toString(),
                    payload_json = json.encodeToString(payloadSerializer, event.payload)
                )
            }
        }
    }

    override suspend fun getNextExpectedEventId(groupId: String): Long {
        return queries.getNextExpectedEventId(groupId).executeAsOne()
    }

    override suspend fun getEventsSince(
        groupId: String,
        eventIdExclusive: Long
    ): List<EventSourcingEvent<*>> {
        return queries.getEventsByGroupSince(groupId, eventIdExclusive).executeAsList().map {
            EventSourcingEvent(
                eventId = it.event_id,
                userId = it.user_id,
                groupId = it.group_id,
                createdAt = Instant.parse(it.created_at),
                payload = json.decodeFromString(payloadSerializer, it.payload_json)
            )
        }
    }

    override suspend fun clearAll() {
        queries.clearAll()
    }

    override suspend fun anonymizeUserIdsIndividually(groupId: String, originalUserId: String) {
        queries.updateUserIdForEvent(
            groupId = groupId,
            originalUserId = originalUserId
        )
    }
}