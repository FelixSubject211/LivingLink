package com.felix.livinglink.server.calendar.domain

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import org.koin.core.annotation.Single
import kotlin.time.Instant

@Single(binds = [ScheduledEventCalculator::class])
class DefaultScheduledEventCalculator : ScheduledEventCalculator {
    override fun calculate(
        event: CalendarEvent,
        from: Instant,
        to: Instant,
        timeZone: TimeZone,
    ): Sequence<ScheduledEvent> {
        require(to >= from) { "to must be >= from" }

        return when (event.recurrence) {
            null -> calculateSingle(event, from, to, timeZone)
            else -> calculateRecurring(event, event.recurrence, from, to, timeZone)
        }
    }

    private fun calculateSingle(
        event: CalendarEvent,
        from: Instant,
        to: Instant,
        timeZone: TimeZone,
    ): Sequence<ScheduledEvent> {
        if (!event.span.intersects(from, to, timeZone)) {
            return emptySequence()
        }
        return sequenceOf(event.toScheduled(event.span))
    }

    private fun calculateRecurring(
        event: CalendarEvent,
        recurrence: RecurrenceRule,
        from: Instant,
        to: Instant,
        timeZone: TimeZone,
    ): Sequence<ScheduledEvent> {
        val baseSpan = event.span

        return generateSequence(0) { it + 1 }
            .takeWhile { index -> recurrence.hasOccurrence(index) }
            .map { index -> baseSpan.shiftedBy(recurrence, index, timeZone) }
            .takeWhile { span ->
                !span.startsAfter(to, timeZone) && !span.startsAfter(recurrence.end, timeZone)
            }.filter { span -> span.intersects(from, to, timeZone) }
            .map { span -> event.toScheduled(span) }
    }

    private fun RecurrenceRule.hasOccurrence(index: Int): Boolean =
        when (val end = end) {
            is RecurrenceRule.RecurrenceEnd.Count -> index < end.occurrences
            is RecurrenceRule.RecurrenceEnd.Never, is RecurrenceRule.RecurrenceEnd.Until -> true
        }

    private fun EventSpan.shiftedBy(
        recurrence: RecurrenceRule,
        index: Int,
        timeZone: TimeZone,
    ): EventSpan {
        if (index == 0) return this
        val steps = recurrence.interval * index

        return when (this) {
            is EventSpan.Timed ->
                EventSpan.Timed(
                    start = start.shifted(recurrence.frequency, steps, timeZone),
                    end = end.shifted(recurrence.frequency, steps, timeZone),
                )

            is EventSpan.AllDay ->
                EventSpan.AllDay(
                    startDate = startDate.shifted(recurrence.frequency, steps),
                    endDate = endDate.shifted(recurrence.frequency, steps),
                )
        }
    }

    private fun EventSpan.intersects(
        from: Instant,
        to: Instant,
        timeZone: TimeZone,
    ): Boolean {
        val spanStart = startInstant(timeZone)
        val spanEnd = endInstant(timeZone)
        return spanEnd >= from && spanStart <= to
    }

    private fun EventSpan.startsAfter(
        instant: Instant,
        timeZone: TimeZone,
    ): Boolean = startInstant(timeZone) > instant

    private fun EventSpan.startsAfter(
        end: RecurrenceRule.RecurrenceEnd,
        timeZone: TimeZone,
    ): Boolean {
        if (end !is RecurrenceRule.RecurrenceEnd.Until) return false
        return when (this) {
            is EventSpan.Timed -> start > end.at
            is EventSpan.AllDay -> endInstant(timeZone) > end.at
        }
    }

    private fun EventSpan.startInstant(timeZone: TimeZone): Instant =
        when (this) {
            is EventSpan.Timed -> start
            is EventSpan.AllDay -> startDate.atStartOfDayIn(timeZone)
        }

    private fun EventSpan.endInstant(timeZone: TimeZone): Instant =
        when (this) {
            is EventSpan.Timed -> end
            is EventSpan.AllDay -> endDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone)
        }

    private fun Instant.shifted(
        frequency: RecurrenceRule.Frequency,
        steps: Int,
        timeZone: TimeZone,
    ): Instant =
        when (frequency) {
            RecurrenceRule.Frequency.Daily -> plus(steps, DateTimeUnit.DAY, timeZone)
            RecurrenceRule.Frequency.Weekly -> plus(steps * 7, DateTimeUnit.DAY, timeZone)
            RecurrenceRule.Frequency.Monthly -> plus(steps, DateTimeUnit.MONTH, timeZone)
            RecurrenceRule.Frequency.Yearly -> plus(steps, DateTimeUnit.YEAR, timeZone)
        }

    private fun LocalDate.shifted(frequency: RecurrenceRule.Frequency, steps: Int): LocalDate =
        when (frequency) {
            RecurrenceRule.Frequency.Daily -> plus(steps, DateTimeUnit.DAY)
            RecurrenceRule.Frequency.Weekly -> plus(steps * 7, DateTimeUnit.DAY)
            RecurrenceRule.Frequency.Monthly -> plus(steps, DateTimeUnit.MONTH)
            RecurrenceRule.Frequency.Yearly -> plus(steps, DateTimeUnit.YEAR)
        }

    private fun CalendarEvent.toScheduled(span: EventSpan): ScheduledEvent =
        ScheduledEvent(
            sourceEventId = id,
            title = title,
            description = description,
            createdByUserId = createdByUserId,
            span = span,
            participants = participants,
            category = category,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
