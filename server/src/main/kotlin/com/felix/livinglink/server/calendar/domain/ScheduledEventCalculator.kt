package com.felix.livinglink.server.calendar.domain

import kotlinx.datetime.TimeZone
import kotlin.time.Instant

interface ScheduledEventCalculator {
    fun calculate(
        event: CalendarEvent,
        from: Instant,
        to: Instant,
        timeZone: TimeZone,
    ): Sequence<ScheduledEvent>
}
