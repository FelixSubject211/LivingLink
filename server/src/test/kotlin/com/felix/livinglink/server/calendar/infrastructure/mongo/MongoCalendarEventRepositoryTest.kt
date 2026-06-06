package com.felix.livinglink.server.calendar.infrastructure.mongo

import com.felix.livinglink.server.calendar.domain.CalendarEvent
import com.felix.livinglink.server.calendar.domain.CalendarEventQuery
import com.felix.livinglink.server.calendar.domain.EventCategory
import com.felix.livinglink.server.calendar.domain.EventSpan
import com.felix.livinglink.server.calendar.domain.Participant
import com.felix.livinglink.server.calendar.domain.RecurrenceRule
import com.felix.livinglink.server.core.infrastructure.mongo.AbstractMongoRepositoryTest
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

class MongoCalendarEventRepositoryTest : AbstractMongoRepositoryTest() {
    private lateinit var collection: MongoCollection<MongoCalendarEventDocument>
    private lateinit var repository: MongoCalendarEventRepository

    private val t0 = Instant.fromEpochSeconds(1_700_000_000)

    @BeforeTest
    fun setUpRepository() {
        collection = database.getCollection<MongoCalendarEventDocument>("calendar_events")

        runBlocking {
            collection.drop()
        }

        repository = MongoCalendarEventRepository(collection)
    }

    @Test
    fun `find returns only events of the queried group`() =
        runTest {
            val inGroup1 = timedEvent(id = "in-group-1", start = t0, end = t0 + 1.hours, groupId = "group-1")
            val alsoGroup1 = timedEvent(id = "also-group-1", start = t0, end = t0 + 1.hours, groupId = "group-1")
            val inGroup2 = timedEvent(id = "in-group-2", start = t0, end = t0 + 1.hours, groupId = "group-2")

            insert(inGroup1, alsoGroup1, inGroup2)

            val result =
                repository.find(
                    CalendarEventQuery(
                        groupId = "group-1",
                        from = t0 - 1.hours,
                        to = t0 + 2.hours,
                    ),
                )

            assertEquals(
                setOf("in-group-1", "also-group-1"),
                result.toList().map { it.id }.toSet(),
            )
        }

    @Test
    fun `find returns events whose effective window intersects the query window`() =
        runTest {
            val before = timedEvent(id = "before", start = t0 - 5.days, end = t0 - 4.days)
            val touchingStart = timedEvent(id = "touching-start", start = t0 - 1.hours, end = t0)
            val inside = timedEvent(id = "inside", start = t0 + 1.hours, end = t0 + 2.hours)
            val touchingEnd = timedEvent(id = "touching-end", start = t0 + 1.days, end = t0 + 1.days + 1.hours)
            val after = timedEvent(id = "after", start = t0 + 5.days, end = t0 + 6.days)

            insert(before, touchingStart, inside, touchingEnd, after)

            val result =
                repository.find(
                    CalendarEventQuery(
                        groupId = "group-1",
                        from = t0,
                        to = t0 + 1.days,
                    ),
                )

            assertEquals(
                setOf("touching-start", "inside", "touching-end"),
                result.toList().map { it.id }.toSet(),
            )
        }

    @Test
    fun `find returns recurring events whose effectiveTo is open-ended`() =
        runTest {
            val recurring =
                timedEvent(
                    id = "recurring",
                    start = t0 - 30.days,
                    end = t0 - 30.days + 1.hours,
                    recurrence =
                        RecurrenceRule(
                            frequency = RecurrenceRule.Frequency.Weekly,
                            interval = 1,
                            end = RecurrenceRule.RecurrenceEnd.Never,
                        ),
                )

            insert(recurring)

            val result =
                repository.find(
                    CalendarEventQuery(
                        groupId = "group-1",
                        from = t0 + 365.days,
                        to = t0 + 366.days,
                    ),
                )

            assertEquals(listOf("recurring"), result.toList().map { it.id })
        }

    @Test
    fun `find excludes recurring events whose Until end is before the query window`() =
        runTest {
            val terminated =
                timedEvent(
                    id = "terminated",
                    start = t0 - 30.days,
                    end = t0 - 30.days + 1.hours,
                    recurrence =
                        RecurrenceRule(
                            frequency = RecurrenceRule.Frequency.Weekly,
                            interval = 1,
                            end = RecurrenceRule.RecurrenceEnd.Until(at = t0 - 1.days),
                        ),
                )

            insert(terminated)

            val result =
                repository.find(
                    CalendarEventQuery(
                        groupId = "group-1",
                        from = t0,
                        to = t0 + 7.days,
                    ),
                )

            assertEquals(emptyList(), result.toList().map { it.id })
        }

    @Test
    fun `find filters by participantUserIds`() =
        runTest {
            val withAlice = timedEvent(id = "with-alice", start = t0, end = t0 + 1.hours, participantIds = listOf("alice"))
            val withBob = timedEvent(id = "with-bob", start = t0, end = t0 + 1.hours, participantIds = listOf("bob"))
            val withBoth = timedEvent(id = "with-both", start = t0, end = t0 + 1.hours, participantIds = listOf("alice", "bob"))
            val withNobody = timedEvent(id = "with-nobody", start = t0, end = t0 + 1.hours)

            insert(withAlice, withBob, withBoth, withNobody)

            val result =
                repository.find(
                    CalendarEventQuery(
                        groupId = "group-1",
                        from = t0 - 1.hours,
                        to = t0 + 2.hours,
                        participantUserIds = setOf("alice"),
                    ),
                )

            assertEquals(
                setOf("with-alice", "with-both"),
                result.toList().map { it.id }.toSet(),
            )
        }

    @Test
    fun `find filters by createdByUserIds`() =
        runTest {
            val byAlice = timedEvent(id = "by-alice", start = t0, end = t0 + 1.hours, createdBy = "alice")
            val byBob = timedEvent(id = "by-bob", start = t0, end = t0 + 1.hours, createdBy = "bob")

            insert(byAlice, byBob)

            val result =
                repository.find(
                    CalendarEventQuery(
                        groupId = "group-1",
                        from = t0 - 1.hours,
                        to = t0 + 2.hours,
                        createdByUserIds = setOf("alice"),
                    ),
                )

            assertEquals(listOf("by-alice"), result.toList().map { it.id })
        }

    @Test
    fun `find combines time window, participant and creator filters`() =
        runTest {
            val match =
                timedEvent(
                    id = "match",
                    start = t0,
                    end = t0 + 1.hours,
                    createdBy = "alice",
                    participantIds = listOf("bob"),
                )
            val wrongCreator =
                timedEvent(
                    id = "wrong-creator",
                    start = t0,
                    end = t0 + 1.hours,
                    createdBy = "carol",
                    participantIds = listOf("bob"),
                )
            val wrongParticipant =
                timedEvent(
                    id = "wrong-participant",
                    start = t0,
                    end = t0 + 1.hours,
                    createdBy = "alice",
                    participantIds = listOf("dave"),
                )
            val outsideWindow =
                timedEvent(
                    id = "outside-window",
                    start = t0 + 10.days,
                    end = t0 + 10.days + 1.hours,
                    createdBy = "alice",
                    participantIds = listOf("bob"),
                )

            insert(match, wrongCreator, wrongParticipant, outsideWindow)

            val result =
                repository.find(
                    CalendarEventQuery(
                        groupId = "group-1",
                        from = t0 - 1.hours,
                        to = t0 + 2.hours,
                        participantUserIds = setOf("bob"),
                        createdByUserIds = setOf("alice"),
                    ),
                )

            assertEquals(listOf("match"), result.toList().map { it.id })
        }

    @Test
    fun `find ignores empty filter sets and returns all events in the window`() =
        runTest {
            val event1 = timedEvent(id = "1", start = t0, end = t0 + 1.hours, createdBy = "alice")
            val event2 = timedEvent(id = "2", start = t0, end = t0 + 1.hours, createdBy = "bob", participantIds = listOf("carol"))

            insert(event1, event2)

            val result =
                repository.find(
                    CalendarEventQuery(
                        groupId = "group-1",
                        from = t0 - 1.hours,
                        to = t0 + 2.hours,
                        participantUserIds = emptySet(),
                        createdByUserIds = emptySet(),
                    ),
                )

            assertEquals(
                setOf("1", "2"),
                result.toList().map { it.id }.toSet(),
            )
        }

    @Test
    fun `find returns mapped domain objects`() =
        runTest {
            val event =
                timedEvent(
                    id = "single",
                    start = t0,
                    end = t0 + 1.hours,
                    createdBy = "alice",
                    participantIds = listOf("bob"),
                )

            insert(event)

            val result =
                repository
                    .find(
                        CalendarEventQuery(
                            groupId = "group-1",
                            from = t0 - 1.hours,
                            to = t0 + 2.hours,
                        ),
                    ).toList()

            assertEquals(listOf(event), result)
        }

    @Test
    fun `findDistinctCustomCategoryLabels returns sorted distinct labels of the group's custom categories`() =
        runTest {
            insert(
                timedEvent(id = "1", start = t0, end = t0 + 1.hours, category = EventCategory.Custom("Urlaub")),
                timedEvent(id = "2", start = t0, end = t0 + 1.hours, category = EventCategory.Custom("Besuch")),
                timedEvent(id = "3", start = t0, end = t0 + 1.hours, category = EventCategory.Custom("Urlaub")),
                timedEvent(id = "4", start = t0, end = t0 + 1.hours, category = EventCategory.None),
                timedEvent(id = "5", start = t0, end = t0 + 1.hours, category = EventCategory.Shopping(shoppingListItemIds = listOf("item-1"))),
                // different group, must never appear
                timedEvent(id = "6", start = t0, end = t0 + 1.hours, groupId = "group-2", category = EventCategory.Custom("Arzt")),
            )

            val result = repository.findDistinctCustomCategoryLabels("group-1")

            assertEquals(listOf("Besuch", "Urlaub"), result)
        }

    @Test
    fun `findDistinctCustomCategoryLabels returns empty list when no custom categories exist`() =
        runTest {
            insert(
                timedEvent(id = "1", start = t0, end = t0 + 1.hours, category = EventCategory.None),
            )

            val result = repository.findDistinctCustomCategoryLabels("group-1")

            assertEquals(emptyList(), result)
        }

    private suspend fun insert(vararg events: CalendarEvent) {
        collection.insertMany(
            events.map { MongoCalendarEventDocument.fromDomain(it) },
        )
    }

    private fun timedEvent(
        id: String,
        start: Instant,
        end: Instant,
        groupId: String = "group-1",
        createdBy: String = "creator",
        participantIds: List<String> = emptyList(),
        recurrence: RecurrenceRule? = null,
        category: EventCategory = EventCategory.None,
    ): CalendarEvent =
        CalendarEvent(
            id = id,
            groupId = groupId,
            title = "event-$id",
            description = null,
            createdByUserId = createdBy,
            span = EventSpan.Timed(start = start, end = end),
            recurrence = recurrence,
            participants =
                participantIds.map { userId ->
                    Participant(userId = userId, rsvpEvents = emptyList())
                },
            category = category,
            createdAt = start,
            updatedAt = start,
        )
}
