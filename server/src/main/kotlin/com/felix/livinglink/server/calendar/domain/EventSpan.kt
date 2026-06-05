package com.felix.livinglink.server.calendar.domain

import kotlinx.datetime.LocalDate
import kotlin.time.Instant

sealed interface EventSpan {
    data class Timed(
        val start: Instant,
        val end: Instant,
    ) : EventSpan {
        init {
            require(end >= start) { "Timed.end must be >= start" }
        }
    }

    data class AllDay(
        val startDate: LocalDate,
        val endDate: LocalDate,
    ) : EventSpan {
        init {
            require(endDate >= startDate) { "AllDay.endDate must be >= startDate" }
        }
    }
}
