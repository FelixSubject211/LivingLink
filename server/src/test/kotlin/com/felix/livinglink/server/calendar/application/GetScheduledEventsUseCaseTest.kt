package com.felix.livinglink.server.calendar.application

import com.felix.livinglink.server.calendar.application.GetScheduledEventsUseCase
import com.felix.livinglink.server.calendar.domain.CalendarEvent
import com.felix.livinglink.server.calendar.domain.CalendarEventQuery
import com.felix.livinglink.server.calendar.domain.CalendarEventRepository
import com.felix.livinglink.server.calendar.domain.CalendarEventSort
import com.felix.livinglink.server.calendar.domain.EventCategory
import com.felix.livinglink.server.calendar.domain.EventSpan
import com.felix.livinglink.server.calendar.domain.ScheduledEvent
import com.felix.livinglink.server.calendar.domain.ScheduledEventCalculator
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.exactly
import dev.mokkery.verifySuspend
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

class GetScheduledEventsUseCaseTest {
    private val calendarEventRepository = mock<CalendarEventRepository>()
    private val scheduledEventCalculator = mock<ScheduledEventCalculator>()

    private val useCase =
        GetScheduledEventsUseCase(
            calendarEventRepository = calendarEventRepository,
            scheduledEventCalculator = scheduledEventCalculator,
        )

    private val t0 = Instant.fromEpochSeconds(1_700_000_000)
    private val utc = TimeZone.UTC
    private val berlin = TimeZone.of("Europe/Berlin")

    private val query =
        CalendarEventQuery(
            from = t0,
            to = t0 + 30.days,
        )

    @Test
    fun `passes query and timeZone to repository and calculator`() =
        runTest {
            val event = calendarEvent(id = "src-1")
            val occurrence = timedScheduled(sourceEventId = "src-1", start = t0, end = t0 + 1.hours)

            everySuspend { calendarEventRepository.find(query) } returns flowOf(event)
            every { scheduledEventCalculator.calculate(event, query.from, query.to, utc) } returns sequenceOf(occurrence)

            val result =
                useCase(
                    GetScheduledEventsUseCase.Input(
                        query = query,
                        sort = CalendarEventSort.EffectiveStartAscending,
                        timeZone = utc,
                    ),
                )

            assertEquals(listOf(occurrence), result.scheduledEvents)
            verifySuspend(exactly(1)) { calendarEventRepository.find(query) }
        }

    @Test
    fun `flattens occurrences from multiple events`() =
        runTest {
            val eventA = calendarEvent(id = "src-a")
            val eventB = calendarEvent(id = "src-b")

            val a1 = timedScheduled(sourceEventId = "src-a", start = t0, end = t0 + 1.hours)
            val a2 = timedScheduled(sourceEventId = "src-a", start = t0 + 7.days, end = t0 + 7.days + 1.hours)
            val b1 = timedScheduled(sourceEventId = "src-b", start = t0 + 1.days, end = t0 + 1.days + 1.hours)

            everySuspend { calendarEventRepository.find(query) } returns flowOf(eventA, eventB)
            every { scheduledEventCalculator.calculate(eventA, query.from, query.to, utc) } returns sequenceOf(a1, a2)
            every { scheduledEventCalculator.calculate(eventB, query.from, query.to, utc) } returns sequenceOf(b1)

            val result =
                useCase(
                    GetScheduledEventsUseCase.Input(
                        query = query,
                        sort = CalendarEventSort.EffectiveStartAscending,
                        timeZone = utc,
                    ),
                )

            assertEquals(listOf(a1, b1, a2), result.scheduledEvents)
        }

    @Test
    fun `sorts by EffectiveStartAscending`() =
        runTest {
            val first = timedScheduled(sourceEventId = "first", start = t0, end = t0 + 1.hours)
            val middle = timedScheduled(sourceEventId = "middle", start = t0 + 1.days, end = t0 + 1.days + 1.hours)
            val last = timedScheduled(sourceEventId = "last", start = t0 + 2.days, end = t0 + 2.days + 1.hours)

            stubSingleEvent(returning = sequenceOf(last, first, middle))

            val result = invoke(sort = CalendarEventSort.EffectiveStartAscending)

            assertEquals(listOf(first, middle, last), result.scheduledEvents)
        }

    @Test
    fun `sorts by EffectiveStartDescending`() =
        runTest {
            val first = timedScheduled(sourceEventId = "first", start = t0, end = t0 + 1.hours)
            val middle = timedScheduled(sourceEventId = "middle", start = t0 + 1.days, end = t0 + 1.days + 1.hours)
            val last = timedScheduled(sourceEventId = "last", start = t0 + 2.days, end = t0 + 2.days + 1.hours)

            stubSingleEvent(returning = sequenceOf(first, last, middle))

            val result = invoke(sort = CalendarEventSort.EffectiveStartDescending)

            assertEquals(listOf(last, middle, first), result.scheduledEvents)
        }

    @Test
    fun `sorts by CreatedAtAscending`() =
        runTest {
            val first = timedScheduled(sourceEventId = "first", start = t0, end = t0 + 1.hours, createdAt = t0 - 5.days)
            val middle = timedScheduled(sourceEventId = "middle", start = t0, end = t0 + 1.hours, createdAt = t0 - 1.days)
            val last = timedScheduled(sourceEventId = "last", start = t0, end = t0 + 1.hours, createdAt = t0)

            stubSingleEvent(returning = sequenceOf(last, first, middle))

            val result = invoke(sort = CalendarEventSort.CreatedAtAscending)

            assertEquals(listOf(first, middle, last), result.scheduledEvents)
        }

    @Test
    fun `sorts by CreatedAtDescending`() =
        runTest {
            val first = timedScheduled(sourceEventId = "first", start = t0, end = t0 + 1.hours, createdAt = t0 - 5.days)
            val middle = timedScheduled(sourceEventId = "middle", start = t0, end = t0 + 1.hours, createdAt = t0 - 1.days)
            val last = timedScheduled(sourceEventId = "last", start = t0, end = t0 + 1.hours, createdAt = t0)

            stubSingleEvent(returning = sequenceOf(first, last, middle))

            val result = invoke(sort = CalendarEventSort.CreatedAtDescending)

            assertEquals(listOf(last, middle, first), result.scheduledEvents)
        }

    @Test
    fun `EffectiveStart sort uses the given timeZone to resolve AllDay spans`() =
        runTest {
            val timed =
                timedScheduled(
                    sourceEventId = "timed",
                    start = Instant.parse("2023-12-31T23:30:00Z"),
                    end = Instant.parse("2024-01-01T00:30:00Z"),
                )
            val allDay =
                allDayScheduled(
                    sourceEventId = "all-day",
                    startDate = LocalDate(2024, 1, 1),
                    endDate = LocalDate(2024, 1, 1),
                )

            stubSingleEvent(returning = sequenceOf(timed, allDay))

            val inUtc = invoke(sort = CalendarEventSort.EffectiveStartAscending, timeZone = utc)
            assertEquals(listOf(timed, allDay), inUtc.scheduledEvents)

            val inBerlin = invoke(sort = CalendarEventSort.EffectiveStartAscending, timeZone = berlin)
            assertEquals(listOf(allDay, timed), inBerlin.scheduledEvents)
        }

    @Test
    fun `returns empty output when repository yields no events`() =
        runTest {
            everySuspend { calendarEventRepository.find(any()) } returns flowOf()

            val result = invoke(sort = CalendarEventSort.EffectiveStartAscending)

            assertEquals(emptyList(), result.scheduledEvents)
        }

    @Test
    fun `returns empty output when calculator produces no occurrences`() =
        runTest {
            stubSingleEvent(returning = emptySequence())

            val result = invoke(sort = CalendarEventSort.EffectiveStartAscending)

            assertEquals(emptyList(), result.scheduledEvents)
        }

    @Test
    fun `fails when the total number of occurrences exceeds MAX_RESULTS`() =
        runTest {
            val eventA = calendarEvent(id = "src-a")
            val eventB = calendarEvent(id = "src-b")
            val eventC = calendarEvent(id = "src-c")

            everySuspend { calendarEventRepository.find(query) } returns flowOf(eventA, eventB, eventC)
            every { scheduledEventCalculator.calculate(eventA, query.from, query.to, utc) } returns
                occurrenceSequence(sourceEventId = "src-a", count = 4_000)
            every { scheduledEventCalculator.calculate(eventB, query.from, query.to, utc) } returns
                occurrenceSequence(sourceEventId = "src-b", count = 4_000)
            every { scheduledEventCalculator.calculate(eventC, query.from, query.to, utc) } returns
                occurrenceSequence(sourceEventId = "src-c", count = 4_000)

            assertFailsWith<IllegalArgumentException> {
                invoke(sort = CalendarEventSort.EffectiveStartAscending)
            }
        }

    private suspend fun invoke(
        sort: CalendarEventSort,
        timeZone: TimeZone = utc,
    ): GetScheduledEventsUseCase.Output =
        useCase(
            GetScheduledEventsUseCase.Input(
                query = query,
                sort = sort,
                timeZone = timeZone,
            ),
        )

    private fun stubSingleEvent(returning: Sequence<ScheduledEvent>) {
        val event = calendarEvent(id = "src-1")
        everySuspend { calendarEventRepository.find(any()) } returns flowOf(event)
        every {
            scheduledEventCalculator.calculate(any(), any(), any(), any())
        } returns returning
    }

    private fun calendarEvent(id: String): CalendarEvent =
        CalendarEvent(
            id = id,
            title = "event-$id",
            description = null,
            createdByUserId = "creator",
            span = EventSpan.Timed(start = t0, end = t0 + 1.hours),
            recurrence = null,
            participants = emptyList(),
            category = EventCategory.None,
            createdAt = t0,
            updatedAt = t0,
        )

    private fun timedScheduled(
        sourceEventId: String,
        start: Instant,
        end: Instant,
        createdAt: Instant = t0,
    ): ScheduledEvent =
        ScheduledEvent(
            sourceEventId = sourceEventId,
            title = "event-$sourceEventId",
            description = null,
            createdByUserId = "creator",
            span = EventSpan.Timed(start = start, end = end),
            participants = emptyList(),
            category = EventCategory.None,
            createdAt = createdAt,
            updatedAt = createdAt,
        )

    private fun allDayScheduled(
        sourceEventId: String,
        startDate: LocalDate,
        endDate: LocalDate,
    ): ScheduledEvent =
        ScheduledEvent(
            sourceEventId = sourceEventId,
            title = "event-$sourceEventId",
            description = null,
            createdByUserId = "creator",
            span = EventSpan.AllDay(startDate = startDate, endDate = endDate),
            participants = emptyList(),
            category = EventCategory.None,
            createdAt = t0,
            updatedAt = t0,
        )

    private fun occurrenceSequence(
        sourceEventId: String,
        count: Int,
    ): Sequence<ScheduledEvent> =
        generateSequence(0) { it + 1 }
            .take(count)
            .map { index ->
                timedScheduled(
                    sourceEventId = sourceEventId,
                    start = t0 + index.hours,
                    end = t0 + index.hours + 1.hours,
                )
            }
}
