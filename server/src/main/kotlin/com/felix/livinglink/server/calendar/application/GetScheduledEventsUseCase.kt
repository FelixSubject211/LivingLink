package com.felix.livinglink.server.calendar.application

import com.felix.livinglink.server.calendar.domain.CalendarEventQuery
import com.felix.livinglink.server.calendar.domain.CalendarEventRepository
import com.felix.livinglink.server.calendar.domain.CalendarEventSort
import com.felix.livinglink.server.calendar.domain.EventSpan
import com.felix.livinglink.server.calendar.domain.ScheduledEvent
import com.felix.livinglink.server.calendar.domain.ScheduledEventCalculator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import org.koin.core.annotation.Single
import kotlin.time.Instant

@Single
class GetScheduledEventsUseCase(
    private val calendarEventRepository: CalendarEventRepository,
    private val scheduledEventCalculator: ScheduledEventCalculator,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend operator fun invoke(input: Input): Output {
        val capped =
            calendarEventRepository
                .find(input.query)
                .flatMapConcat { event ->
                    scheduledEventCalculator
                        .calculate(
                            event = event,
                            from = input.query.from,
                            to = input.query.to,
                            timeZone = input.timeZone,
                        ).asFlow()
                }.take(MAX_RESULTS + 1)
                .toList()

        require(capped.size <= MAX_RESULTS) {
            "Calendar query produced more than $MAX_RESULTS scheduled events. Narrow the time range or filters."
        }

        val sorted = capped.sortedWith(comparatorFor(input.sort, input.timeZone))

        return Output(scheduledEvents = sorted)
    }

    private fun comparatorFor(
        sort: CalendarEventSort,
        timeZone: TimeZone,
    ): Comparator<ScheduledEvent> =
        when (sort) {
            CalendarEventSort.EffectiveStartAscending ->
                compareBy { it.span.startInstant(timeZone) }

            CalendarEventSort.EffectiveStartDescending ->
                compareByDescending { it.span.startInstant(timeZone) }

            CalendarEventSort.CreatedAtAscending ->
                compareBy { it.createdAt }

            CalendarEventSort.CreatedAtDescending ->
                compareByDescending { it.createdAt }
        }

    private fun EventSpan.startInstant(timeZone: TimeZone): Instant =
        when (this) {
            is EventSpan.Timed -> start
            is EventSpan.AllDay -> startDate.atStartOfDayIn(timeZone)
        }

    data class Input(
        val query: CalendarEventQuery,
        val sort: CalendarEventSort,
        val timeZone: TimeZone,
    )

    data class Output(
        val scheduledEvents: List<ScheduledEvent>,
    )

    companion object {
        private const val MAX_RESULTS = 10_000
    }
}
