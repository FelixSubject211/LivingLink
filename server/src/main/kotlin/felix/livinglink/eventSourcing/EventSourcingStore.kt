package felix.livinglink.eventSourcing

import felix.livinglink.common.EventCountersTable
import felix.livinglink.common.EventSourcingEventsTable
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.asc
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.gt
import org.ktorm.dsl.insert
import org.ktorm.dsl.map
import org.ktorm.dsl.orderBy
import org.ktorm.dsl.plus
import org.ktorm.dsl.select
import org.ktorm.dsl.update
import org.ktorm.dsl.where

interface EventSourcingStore {
    fun appendEvent(
        groupId: String,
        userId: String,
        eventType: String,
        createdAt: Instant,
        payload: String
    ): Long?

    fun getEventsSince(groupId: String, sinceEventIdExclusive: Long?): List<Event>

    data class Event(
        val groupId: String,
        val eventId: Long,
        val eventType: String,
        val userId: String,
        val createdAt: Instant,
        val payload: String
    )
}

class EventSourcingDefaultStore(private val database: Database) : EventSourcingStore {

    override fun appendEvent(
        groupId: String,
        userId: String,
        eventType: String,
        createdAt: Instant,
        payload: String
    ): Long? {
        return database.useTransaction {
            val updated = database.update(EventCountersTable) {
                set(it.lastEventId, it.lastEventId + 1)
                where { it.groupId eq groupId }
            }

            val nextEventId: Long = if (updated > 0) {
                database
                    .from(EventCountersTable)
                    .select(EventCountersTable.lastEventId)
                    .where { EventCountersTable.groupId eq groupId }
                    .map { it[EventCountersTable.lastEventId]!! }
                    .first()
            } else {
                database.insert(EventCountersTable) {
                    set(it.groupId, groupId)
                    set(it.lastEventId, 1L)
                }
                1L
            }

            val inserted = database.insert(EventSourcingEventsTable) {
                set(it.groupId, groupId)
                set(it.eventId, nextEventId)
                set(it.eventType, eventType)
                set(it.userId, userId)
                set(it.createdAt, createdAt.toJavaInstant())
                set(it.payload, payload)
            }

            if (inserted > 0) nextEventId else null
        }
    }


    override fun getEventsSince(
        groupId: String,
        sinceEventIdExclusive: Long?
    ): List<EventSourcingStore.Event> {
        return database
            .from(EventSourcingEventsTable)
            .select()
            .where {
                (EventSourcingEventsTable.groupId eq groupId) and
                        (EventSourcingEventsTable.eventId gt (sinceEventIdExclusive ?: 0))
            }
            .orderBy(EventSourcingEventsTable.eventId.asc())
            .map { row ->
                EventSourcingStore.Event(
                    groupId = row[EventSourcingEventsTable.groupId]!!,
                    eventId = row[EventSourcingEventsTable.eventId]!!,
                    eventType = row[EventSourcingEventsTable.eventType]!!,
                    userId = row[EventSourcingEventsTable.userId]!!,
                    createdAt = row[EventSourcingEventsTable.createdAt]!!.toKotlinInstant(),
                    payload = row[EventSourcingEventsTable.payload]!!
                )
            }
    }
}