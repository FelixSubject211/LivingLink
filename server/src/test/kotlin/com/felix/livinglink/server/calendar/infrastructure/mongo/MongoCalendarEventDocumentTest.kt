package com.felix.livinglink.server.calendar.infrastructure.mongo

import com.felix.livinglink.server.calendar.domain.CalendarEvent
import com.felix.livinglink.server.calendar.domain.EventCategory
import com.felix.livinglink.server.calendar.domain.EventSpan
import com.felix.livinglink.server.calendar.domain.Participant
import com.felix.livinglink.server.calendar.domain.RecurrenceRule
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

class MongoCalendarEventDocumentTest {
    private val t0 = Instant.fromEpochSeconds(1_700_000_000)

    @Test
    fun `fromDomain maps every field for a timed, non-recurring event`() {
        val event =
            calendarEvent(
                id = "id-1",
                groupId = "group-1",
                title = "Dinner",
                description = "with the in-laws",
                createdByUserId = "creator",
                span = EventSpan.Timed(start = t0, end = t0 + 2.hours),
                recurrence = null,
                participants =
                    listOf(
                        Participant(
                            userId = "user-a",
                            rsvpEvents =
                                listOf(
                                    Participant.RsvpEvent(
                                        status = Participant.RsvpStatus.Accepted,
                                        at = t0,
                                    ),
                                    Participant.RsvpEvent(
                                        status = Participant.RsvpStatus.Declined,
                                        at = t0 + 1.hours,
                                    ),
                                ),
                        ),
                    ),
                category = EventCategory.None,
                createdAt = t0,
                updatedAt = t0 + 1.hours,
                version = 7L,
            )

        val expected =
            MongoCalendarEventDocument(
                id = "id-1",
                groupId = "group-1",
                title = "Dinner",
                description = "with the in-laws",
                createdByUserId = "creator",
                span =
                    MongoCalendarEventDocument.Span(
                        type = MongoCalendarEventDocument.Span.TYPE_TIMED,
                        start = t0,
                        end = t0 + 2.hours,
                        startDate = null,
                        endDate = null,
                    ),
                recurrence = null,
                participants =
                    listOf(
                        MongoCalendarEventDocument.Participant(
                            userId = "user-a",
                            rsvpEvents =
                                listOf(
                                    MongoCalendarEventDocument.Participant.RsvpEvent(
                                        status = "Accepted",
                                        at = t0,
                                    ),
                                    MongoCalendarEventDocument.Participant.RsvpEvent(
                                        status = "Declined",
                                        at = t0 + 1.hours,
                                    ),
                                ),
                        ),
                    ),
                category =
                    MongoCalendarEventDocument.Category(
                        type = MongoCalendarEventDocument.Category.TYPE_NONE,
                        label = null,
                        shoppingListItemIds = null,
                    ),
                effectiveFrom = t0,
                effectiveTo = t0 + 2.hours,
                createdAt = t0,
                updatedAt = t0 + 1.hours,
                version = 7L,
            )

        assertEquals(expected, MongoCalendarEventDocument.fromDomain(event))
    }

    @Test
    fun `fromDomain derives effectiveFrom and effectiveTo for an all-day, non-recurring event`() {
        val startDate = LocalDate(2026, 5, 24)
        val endDate = LocalDate(2026, 5, 26)

        val event =
            calendarEvent(
                span = EventSpan.AllDay(startDate = startDate, endDate = endDate),
            )

        val document = MongoCalendarEventDocument.fromDomain(event)

        assertEquals(
            MongoCalendarEventDocument.Span(
                type = MongoCalendarEventDocument.Span.TYPE_ALL_DAY,
                start = null,
                end = null,
                startDate = "2026-05-24",
                endDate = "2026-05-26",
            ),
            document.span,
        )
        assertEquals(startDate.atStartOfDayIn(TimeZone.UTC), document.effectiveFrom)
        assertEquals(
            endDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(TimeZone.UTC),
            document.effectiveTo,
        )
    }

    @Test
    fun `fromDomain sets effectiveTo to DISTANT_FUTURE for recurrence end Never`() {
        val event =
            calendarEvent(
                span = EventSpan.Timed(start = t0, end = t0 + 1.hours),
                recurrence =
                    RecurrenceRule(
                        frequency = RecurrenceRule.Frequency.Weekly,
                        interval = 1,
                        end = RecurrenceRule.RecurrenceEnd.Never,
                    ),
            )

        val document = MongoCalendarEventDocument.fromDomain(event)

        assertEquals(Instant.DISTANT_FUTURE, document.effectiveTo)
        assertEquals(
            MongoCalendarEventDocument.Recurrence.End(
                type = MongoCalendarEventDocument.Recurrence.End.TYPE_NEVER,
                at = null,
                occurrences = null,
            ),
            document.recurrence?.end,
        )
    }

    @Test
    fun `fromDomain sets effectiveTo to the Until instant for recurrence end Until`() {
        val until = t0 + 30.days

        val event =
            calendarEvent(
                span = EventSpan.Timed(start = t0, end = t0 + 1.hours),
                recurrence =
                    RecurrenceRule(
                        frequency = RecurrenceRule.Frequency.Daily,
                        interval = 2,
                        end = RecurrenceRule.RecurrenceEnd.Until(at = until),
                    ),
            )

        val document = MongoCalendarEventDocument.fromDomain(event)

        assertEquals(until, document.effectiveTo)
        assertEquals(
            MongoCalendarEventDocument.Recurrence(
                frequency = "Daily",
                interval = 2,
                end =
                    MongoCalendarEventDocument.Recurrence.End(
                        type = MongoCalendarEventDocument.Recurrence.End.TYPE_UNTIL,
                        at = until,
                        occurrences = null,
                    ),
            ),
            document.recurrence,
        )
    }

    @Test
    fun `fromDomain sets effectiveTo to DISTANT_FUTURE for recurrence end Count`() {
        val event =
            calendarEvent(
                span = EventSpan.Timed(start = t0, end = t0 + 1.hours),
                recurrence =
                    RecurrenceRule(
                        frequency = RecurrenceRule.Frequency.Monthly,
                        interval = 1,
                        end = RecurrenceRule.RecurrenceEnd.Count(occurrences = 5),
                    ),
            )

        val document = MongoCalendarEventDocument.fromDomain(event)

        assertEquals(Instant.DISTANT_FUTURE, document.effectiveTo)
        assertEquals(
            MongoCalendarEventDocument.Recurrence.End(
                type = MongoCalendarEventDocument.Recurrence.End.TYPE_COUNT,
                at = null,
                occurrences = 5,
            ),
            document.recurrence?.end,
        )
    }

    @Test
    fun `fromDomain maps category Custom with its label`() {
        val event = calendarEvent(category = EventCategory.Custom(label = "Birthday"))

        val document = MongoCalendarEventDocument.fromDomain(event)

        assertEquals(
            MongoCalendarEventDocument.Category(
                type = MongoCalendarEventDocument.Category.TYPE_CUSTOM,
                label = "Birthday",
                shoppingListItemIds = null,
            ),
            document.category,
        )
    }

    @Test
    fun `fromDomain maps category Shopping with its shopping list item ids`() {
        val event =
            calendarEvent(
                category = EventCategory.Shopping(shoppingListItemIds = listOf("item-1", "item-2")),
            )

        val document = MongoCalendarEventDocument.fromDomain(event)

        assertEquals(
            MongoCalendarEventDocument.Category(
                type = MongoCalendarEventDocument.Category.TYPE_SHOPPING,
                label = null,
                shoppingListItemIds = listOf("item-1", "item-2"),
            ),
            document.category,
        )
    }

    @Test
    fun `roundtrip preserves a timed event with full recurrence, participants and category`() {
        val event =
            calendarEvent(
                id = "id-1",
                groupId = "group-1",
                title = "Weekly sync",
                description = "team standup",
                createdByUserId = "creator",
                span = EventSpan.Timed(start = t0, end = t0 + 1.hours),
                recurrence =
                    RecurrenceRule(
                        frequency = RecurrenceRule.Frequency.Weekly,
                        interval = 1,
                        end = RecurrenceRule.RecurrenceEnd.Until(at = t0 + 90.days),
                    ),
                participants =
                    listOf(
                        Participant(
                            userId = "user-a",
                            rsvpEvents =
                                listOf(
                                    Participant.RsvpEvent(
                                        status = Participant.RsvpStatus.Pending,
                                        at = t0,
                                    ),
                                    Participant.RsvpEvent(
                                        status = Participant.RsvpStatus.Accepted,
                                        at = t0 + 1.hours,
                                    ),
                                ),
                        ),
                        Participant(
                            userId = "user-b",
                            rsvpEvents =
                                listOf(
                                    Participant.RsvpEvent(
                                        status = Participant.RsvpStatus.Maybe,
                                        at = t0,
                                    ),
                                ),
                        ),
                    ),
                category = EventCategory.Shopping(shoppingListItemIds = listOf("item-1", "item-2")),
                createdAt = t0,
                updatedAt = t0 + 2.hours,
                version = 4L,
            )

        val roundtripped = MongoCalendarEventDocument.fromDomain(event).toDomain()

        assertEquals(event, roundtripped)
    }

    @Test
    fun `roundtrip preserves an all-day event without recurrence, participants or category`() {
        val event =
            calendarEvent(
                id = "id-2",
                groupId = "group-1",
                title = "Holiday",
                description = null,
                span =
                    EventSpan.AllDay(
                        startDate = LocalDate(2026, 5, 24),
                        endDate = LocalDate(2026, 5, 26),
                    ),
                recurrence = null,
                participants = emptyList(),
                category = EventCategory.None,
                version = 0L,
            )

        val roundtripped = MongoCalendarEventDocument.fromDomain(event).toDomain()

        assertEquals(event, roundtripped)
    }

    @Test
    fun `roundtrip preserves a recurrence with Count end`() {
        val event =
            calendarEvent(
                span = EventSpan.Timed(start = t0, end = t0 + 1.hours),
                recurrence =
                    RecurrenceRule(
                        frequency = RecurrenceRule.Frequency.Yearly,
                        interval = 1,
                        end = RecurrenceRule.RecurrenceEnd.Count(occurrences = 10),
                    ),
            )

        val roundtripped = MongoCalendarEventDocument.fromDomain(event).toDomain()

        assertEquals(event, roundtripped)
    }

    @Test
    fun `withVersion returns a copy with the new version`() {
        val document = MongoCalendarEventDocument.fromDomain(calendarEvent(version = 2L))

        assertEquals(document.copy(version = 5L), document.withVersion(5L))
    }

    private fun calendarEvent(
        id: String = "id-1",
        groupId: String = "group-1",
        title: String = "event-$id",
        description: String? = null,
        createdByUserId: String = "creator",
        span: EventSpan = EventSpan.Timed(start = t0, end = t0 + 1.hours),
        recurrence: RecurrenceRule? = null,
        participants: List<Participant> = emptyList(),
        category: EventCategory = EventCategory.None,
        createdAt: Instant = t0,
        updatedAt: Instant = t0,
        version: Long = 0L,
    ): CalendarEvent =
        CalendarEvent(
            id = id,
            groupId = groupId,
            title = title,
            description = description,
            createdByUserId = createdByUserId,
            span = span,
            recurrence = recurrence,
            participants = participants,
            category = category,
            createdAt = createdAt,
            updatedAt = updatedAt,
            version = version,
        )
}
