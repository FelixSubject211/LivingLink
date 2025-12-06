package felix.projekt.livinglink.composeApp.eventSourcing.infrastructure

import felix.projekt.livinglink.composeApp.eventDatabase.EventDatabase
import felix.projekt.livinglink.composeApp.eventSourcing.domain.EventStore
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.EventSourcingEvent
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.TopicSubscription
import felix.projekt.livinglink.shared.json
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonElement

class SqlDelightEventStore(
    private val database: EventDatabase,
) : EventStore {

    private val mutex = Mutex()

    override suspend fun append(
        subscription: TopicSubscription<*>,
        events: List<EventSourcingEvent>
    ) = mutex.withLock {
        if (events.isEmpty()) {
            return@withLock
        }
        val groupId = subscription.groupId
        val topicValue = subscription.topic.value
        val queries = database.eventDatabaseQueries

        val lastKnown = queries.lastEventId(
            groupId = groupId,
            topic = topicValue
        ).executeAsOne()
        val firstIncoming = events.first().eventId
        val expectedFirst = lastKnown + 1L
        if (firstIncoming != expectedFirst) {
            throw IllegalArgumentException(
                "Event ordering violation: got $firstIncoming but expected $expectedFirst"
            )
        }
        events.zipWithNext().forEach { (previous, next) ->
            if (next.eventId != previous.eventId + 1L) {
                throw IllegalArgumentException(
                    "Event ordering violation: got ${next.eventId} but expected ${previous.eventId + 1L}"
                )
            }
        }
        events.forEach { event ->
            if (event.groupId != groupId || event.topic != topicValue) {
                throw IllegalArgumentException(
                    "Event topic mismatch for subscription $groupId:$topicValue"
                )
            }
            queries.insertEvent(
                groupId = event.groupId,
                topic = event.topic,
                eventId = event.eventId,
                createdBy = event.createdBy,
                createdAtEpochMillis = event.createdAtEpochMillis,
                payload = json.encodeToString(event.payload)
            )
        }
    }

    override suspend fun lastEventId(subscription: TopicSubscription<*>): Long = mutex.withLock {
        database.eventDatabaseQueries.lastEventId(
            groupId = subscription.groupId,
            topic = subscription.topic.value
        ).executeAsOne()
    }

    override suspend fun eventsSince(
        subscription: TopicSubscription<*>,
        eventId: Long
    ): List<EventSourcingEvent> = mutex.withLock {
        database.eventDatabaseQueries.eventsSince(
            groupId = subscription.groupId,
            topic = subscription.topic.value,
            eventId = eventId
        ).executeAsList().map { row ->
            EventSourcingEvent(
                eventId = row.eventId,
                groupId = row.groupId,
                topic = row.topic,
                createdBy = row.createdBy,
                createdAtEpochMillis = row.createdAtEpochMillis,
                payload = parsePayload(row.payload)
            )
        }
    }

    override suspend fun clearAll() = mutex.withLock {
        database.eventDatabaseQueries.deleteAll()
        Unit
    }

    private fun parsePayload(serialized: String): JsonElement {
        return json.parseToJsonElement(serialized)
    }
}
