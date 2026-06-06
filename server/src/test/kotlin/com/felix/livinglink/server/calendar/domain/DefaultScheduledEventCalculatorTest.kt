package com.felix.livinglink.server.calendar.domain

import com.felix.livinglink.server.calendar.domain.CalendarEvent
import com.felix.livinglink.server.calendar.domain.DefaultScheduledEventCalculator
import com.felix.livinglink.server.calendar.domain.EventCategory
import com.felix.livinglink.server.calendar.domain.EventSpan
import com.felix.livinglink.server.calendar.domain.Participant
import com.felix.livinglink.server.calendar.domain.RecurrenceRule
import com.felix.livinglink.server.calendar.domain.RecurrenceRule.Frequency
import com.felix.livinglink.server.calendar.domain.RecurrenceRule.RecurrenceEnd
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class DefaultScheduledEventCalculatorTest {
    private val calculator = DefaultScheduledEventCalculator()

    private val utc = TimeZone.UTC
    private val berlin = TimeZone.of("Europe/Berlin")

    private val t0 = Instant.fromEpochSeconds(1_700_000_000)

    @Test
    fun `calculate rejects a window where to is before from`() {
        val event = timedEvent(start = t0, end = t0 + 1.hours)

        assertFailsWith<IllegalArgumentException> {
            calculator.calculate(
                event = event,
                from = t0 + 1.hours,
                to = t0,
                timeZone = utc,
            )
        }
    }

    @Test
    fun `single timed event fully inside the window yields one scheduled event`() {
        val event = timedEvent(id = "id-1", start = t0 + 1.hours, end = t0 + 2.hours)

        val result =
            calculator.calculate(
                event = event,
                from = t0,
                to = t0 + 1.days,
                timeZone = utc,
            )

        assertEquals(listOf(event.span), result.toList().map { it.span })
    }

    @Test
    fun `single timed event entirely before the window yields nothing`() {
        val event = timedEvent(start = t0 - 2.hours, end = t0 - 1.hours)

        val result =
            calculator.calculate(
                event = event,
                from = t0,
                to = t0 + 1.days,
                timeZone = utc,
            )

        assertEquals(emptyList(), result.toList())
    }

    @Test
    fun `single timed event entirely after the window yields nothing`() {
        val event = timedEvent(start = t0 + 2.days, end = t0 + 2.days + 1.hours)

        val result =
            calculator.calculate(
                event = event,
                from = t0,
                to = t0 + 1.days,
                timeZone = utc,
            )

        assertEquals(emptyList(), result.toList())
    }

    @Test
    fun `single timed event whose end touches the window start is included`() {
        val event = timedEvent(start = t0 - 1.hours, end = t0)

        val result =
            calculator.calculate(
                event = event,
                from = t0,
                to = t0 + 1.days,
                timeZone = utc,
            )

        assertEquals(1, result.toList().size)
    }

    @Test
    fun `single timed event whose start touches the window end is included`() {
        val event = timedEvent(start = t0 + 1.days, end = t0 + 1.days + 1.hours)

        val result =
            calculator.calculate(
                event = event,
                from = t0,
                to = t0 + 1.days,
                timeZone = utc,
            )

        assertEquals(1, result.toList().size)
    }

    @Test
    fun `single allDay event uses the given timezone for intersection`() {
        val event =
            allDayEvent(
                startDate = LocalDate(2024, 1, 1),
                endDate = LocalDate(2024, 1, 1),
            )

        val before =
            calculator.calculate(
                event = event,
                from = Instant.parse("2023-12-31T22:00:00Z"),
                to = Instant.parse("2023-12-31T22:30:00Z"),
                timeZone = berlin,
            )

        val crossing =
            calculator.calculate(
                event = event,
                from = Instant.parse("2023-12-31T22:30:00Z"),
                to = Instant.parse("2023-12-31T23:30:00Z"),
                timeZone = berlin,
            )

        assertEquals(emptyList(), before.toList())
        assertEquals(1, crossing.toList().size)
    }

    @Test
    fun `single event maps every ScheduledEvent field from the source event`() {
        val event =
            timedEvent(
                id = "src-1",
                title = "Dinner",
                description = "with the in-laws",
                createdByUserId = "creator",
                start = t0,
                end = t0 + 2.hours,
                participants =
                    listOf(
                        Participant(userId = "user-a", rsvpEvents = emptyList()),
                    ),
                category = EventCategory.Custom(label = "Family"),
                createdAt = t0 - 1.days,
                updatedAt = t0 - 1.hours,
            )

        val result =
            calculator
                .calculate(
                    event = event,
                    from = t0 - 1.hours,
                    to = t0 + 3.hours,
                    timeZone = utc,
                ).toList()

        assertEquals(1, result.size)
        val scheduled = result.single()
        assertEquals("src-1", scheduled.sourceEventId)
        assertEquals("Dinner", scheduled.title)
        assertEquals("with the in-laws", scheduled.description)
        assertEquals("creator", scheduled.createdByUserId)
        assertEquals(event.span, scheduled.span)
        assertEquals(event.participants, scheduled.participants)
        assertEquals(event.category, scheduled.category)
        assertEquals(event.createdAt, scheduled.createdAt)
        assertEquals(event.updatedAt, scheduled.updatedAt)
    }

    @Test
    fun `daily recurrence expands one occurrence per day`() {
        val event =
            timedEvent(
                start = t0,
                end = t0 + 1.hours,
                recurrence = recurrence(Frequency.Daily, interval = 1, end = RecurrenceEnd.Count(5)),
            )

        val result =
            calculator
                .calculate(
                    event = event,
                    from = t0,
                    to = t0 + 10.days,
                    timeZone = utc,
                ).toList()

        assertEquals(
            listOf(0, 1, 2, 3, 4).map { i -> t0 + i.days },
            result.map { (it.span as EventSpan.Timed).start },
        )
    }

    @Test
    fun `weekly recurrence shifts by 7 days per step`() {
        val event =
            timedEvent(
                start = t0,
                end = t0 + 1.hours,
                recurrence = recurrence(Frequency.Weekly, interval = 1, end = RecurrenceEnd.Count(3)),
            )

        val result =
            calculator
                .calculate(
                    event = event,
                    from = t0,
                    to = t0 + 30.days,
                    timeZone = utc,
                ).toList()

        assertEquals(
            listOf(0, 7, 14).map { i -> t0 + i.days },
            result.map { (it.span as EventSpan.Timed).start },
        )
    }

    @Test
    fun `monthly recurrence advances by calendar months`() {
        val event =
            allDayEvent(
                startDate = LocalDate(2024, 1, 15),
                endDate = LocalDate(2024, 1, 15),
                recurrence = recurrence(Frequency.Monthly, interval = 1, end = RecurrenceEnd.Count(4)),
            )

        val result =
            calculator
                .calculate(
                    event = event,
                    from = Instant.parse("2024-01-01T00:00:00Z"),
                    to = Instant.parse("2024-12-31T00:00:00Z"),
                    timeZone = utc,
                ).toList()

        assertEquals(
            listOf(
                LocalDate(2024, 1, 15),
                LocalDate(2024, 2, 15),
                LocalDate(2024, 3, 15),
                LocalDate(2024, 4, 15),
            ),
            result.map { (it.span as EventSpan.AllDay).startDate },
        )
    }

    @Test
    fun `yearly recurrence advances by calendar years`() {
        val event =
            allDayEvent(
                startDate = LocalDate(2024, 5, 24),
                endDate = LocalDate(2024, 5, 24),
                recurrence = recurrence(Frequency.Yearly, interval = 1, end = RecurrenceEnd.Count(3)),
            )

        val result =
            calculator
                .calculate(
                    event = event,
                    from = Instant.parse("2024-01-01T00:00:00Z"),
                    to = Instant.parse("2030-01-01T00:00:00Z"),
                    timeZone = utc,
                ).toList()

        assertEquals(
            listOf(
                LocalDate(2024, 5, 24),
                LocalDate(2025, 5, 24),
                LocalDate(2026, 5, 24),
            ),
            result.map { (it.span as EventSpan.AllDay).startDate },
        )
    }

    @Test
    fun `interval greater than one skips occurrences between steps`() {
        val event =
            timedEvent(
                start = t0,
                end = t0 + 1.hours,
                recurrence = recurrence(Frequency.Daily, interval = 3, end = RecurrenceEnd.Count(4)),
            )

        val result =
            calculator
                .calculate(
                    event = event,
                    from = t0,
                    to = t0 + 30.days,
                    timeZone = utc,
                ).toList()

        assertEquals(
            listOf(0, 3, 6, 9).map { i -> t0 + i.days },
            result.map { (it.span as EventSpan.Timed).start },
        )
    }

    @Test
    fun `recurrence with end Count yields at most the given number of occurrences`() {
        val event =
            timedEvent(
                start = t0,
                end = t0 + 1.hours,
                recurrence = recurrence(Frequency.Daily, interval = 1, end = RecurrenceEnd.Count(3)),
            )

        val result =
            calculator
                .calculate(
                    event = event,
                    from = t0,
                    to = t0 + 365.days,
                    timeZone = utc,
                ).toList()

        assertEquals(3, result.size)
    }

    @Test
    fun `recurrence with end Until stops once an occurrence starts after the cutoff`() {
        val event =
            timedEvent(
                start = t0,
                end = t0 + 1.hours,
                recurrence =
                    recurrence(
                        frequency = Frequency.Daily,
                        interval = 1,
                        end = RecurrenceEnd.Until(at = t0 + 2.days + 30.minutes),
                    ),
            )

        val result =
            calculator
                .calculate(
                    event = event,
                    from = t0,
                    to = t0 + 30.days,
                    timeZone = utc,
                ).toList()

        assertEquals(
            listOf(0, 1, 2).map { i -> t0 + i.days },
            result.map { (it.span as EventSpan.Timed).start },
        )
    }

    @Test
    fun `recurrence with end Never expands only within the requested window`() {
        val event =
            timedEvent(
                start = t0,
                end = t0 + 1.hours,
                recurrence = recurrence(Frequency.Daily, interval = 1, end = RecurrenceEnd.Never),
            )

        val result =
            calculator
                .calculate(
                    event = event,
                    from = t0,
                    to = t0 + 5.days,
                    timeZone = utc,
                ).toList()

        assertEquals(6, result.size)
    }

    @Test
    fun `recurrence terminates even with Never end and a tiny window`() {
        val event =
            timedEvent(
                start = t0,
                end = t0 + 1.hours,
                recurrence = recurrence(Frequency.Daily, interval = 1, end = RecurrenceEnd.Never),
            )

        val result =
            calculator
                .calculate(
                    event = event,
                    from = t0,
                    to = t0 + 1.hours,
                    timeZone = utc,
                ).toList()

        assertEquals(1, result.size)
    }

    @Test
    fun `window cuts off occurrences before from and after to`() {
        val event =
            timedEvent(
                start = t0,
                end = t0 + 1.hours,
                recurrence = recurrence(Frequency.Daily, interval = 1, end = RecurrenceEnd.Count(20)),
            )

        val result =
            calculator
                .calculate(
                    event = event,
                    from = t0 + 5.days,
                    to = t0 + 10.days,
                    timeZone = utc,
                ).toList()

        assertEquals(
            listOf(5, 6, 7, 8, 9, 10).map { i -> t0 + i.days },
            result.map { (it.span as EventSpan.Timed).start },
        )
    }

    @Test
    fun `recurring event with no occurrence inside the window yields nothing`() {
        val event =
            timedEvent(
                start = t0,
                end = t0 + 1.hours,
                recurrence = recurrence(Frequency.Daily, interval = 7, end = RecurrenceEnd.Count(20)),
            )

        val result =
            calculator
                .calculate(
                    event = event,
                    from = t0 + 1.days,
                    to = t0 + 6.days,
                    timeZone = utc,
                ).toList()

        assertEquals(emptyList(), result)
    }

    @Test
    fun `every recurring occurrence keeps the source event's metadata`() {
        val event =
            timedEvent(
                id = "src-1",
                title = "Yoga",
                createdByUserId = "creator",
                start = t0,
                end = t0 + 1.hours,
                recurrence = recurrence(Frequency.Daily, interval = 1, end = RecurrenceEnd.Count(3)),
            )

        val result =
            calculator
                .calculate(
                    event = event,
                    from = t0,
                    to = t0 + 10.days,
                    timeZone = utc,
                ).toList()

        assertEquals(3, result.size)
        assertTrue(result.all { it.sourceEventId == "src-1" })
        assertTrue(result.all { it.title == "Yoga" })
        assertTrue(result.all { it.createdByUserId == "creator" })
    }

    @Test
    fun `each recurring occurrence has the shifted span, not the base span`() {
        val event =
            timedEvent(
                start = t0,
                end = t0 + 1.hours,
                recurrence = recurrence(Frequency.Daily, interval = 1, end = RecurrenceEnd.Count(3)),
            )

        val result =
            calculator
                .calculate(
                    event = event,
                    from = t0,
                    to = t0 + 10.days,
                    timeZone = utc,
                ).toList()

        assertEquals(
            listOf(
                EventSpan.Timed(start = t0, end = t0 + 1.hours),
                EventSpan.Timed(start = t0 + 1.days, end = t0 + 1.days + 1.hours),
                EventSpan.Timed(start = t0 + 2.days, end = t0 + 2.days + 1.hours),
            ),
            result.map { it.span },
        )
    }

    @Test
    fun `recurring allDay event preserves duration across occurrences`() {
        val event =
            allDayEvent(
                startDate = LocalDate(2024, 1, 1),
                endDate = LocalDate(2024, 1, 3), // 3-day span
                recurrence = recurrence(Frequency.Weekly, interval = 1, end = RecurrenceEnd.Count(2)),
            )

        val result =
            calculator
                .calculate(
                    event = event,
                    from = Instant.parse("2024-01-01T00:00:00Z"),
                    to = Instant.parse("2024-02-01T00:00:00Z"),
                    timeZone = utc,
                ).toList()

        assertEquals(
            listOf(
                EventSpan.AllDay(startDate = LocalDate(2024, 1, 1), endDate = LocalDate(2024, 1, 3)),
                EventSpan.AllDay(startDate = LocalDate(2024, 1, 8), endDate = LocalDate(2024, 1, 10)),
            ),
            result.map { it.span },
        )
    }

    private fun timedEvent(
        id: String = "id-1",
        groupId: String = "groupId",
        title: String = "event-$id",
        description: String? = null,
        createdByUserId: String = "creator",
        start: Instant,
        end: Instant,
        recurrence: RecurrenceRule? = null,
        participants: List<Participant> = emptyList(),
        category: EventCategory = EventCategory.None,
        createdAt: Instant = start,
        updatedAt: Instant = start,
    ): CalendarEvent =
        CalendarEvent(
            id = id,
            groupId = groupId,
            title = title,
            description = description,
            createdByUserId = createdByUserId,
            span = EventSpan.Timed(start = start, end = end),
            recurrence = recurrence,
            participants = participants,
            category = category,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    private fun allDayEvent(
        id: String = "id-1",
        groupId: String = "groupId",
        title: String = "event-$id",
        startDate: LocalDate,
        endDate: LocalDate,
        recurrence: RecurrenceRule? = null,
        participants: List<Participant> = emptyList(),
        category: EventCategory = EventCategory.None,
    ): CalendarEvent =
        CalendarEvent(
            id = id,
            groupId = groupId,
            title = title,
            description = null,
            createdByUserId = "creator",
            span = EventSpan.AllDay(startDate = startDate, endDate = endDate),
            recurrence = recurrence,
            participants = participants,
            category = category,
            createdAt = t0,
            updatedAt = t0,
        )

    private fun recurrence(
        frequency: Frequency,
        interval: Int = 1,
        end: RecurrenceEnd = RecurrenceEnd.Never,
    ): RecurrenceRule =
        RecurrenceRule(
            frequency = frequency,
            interval = interval,
            end = end,
        )
}
