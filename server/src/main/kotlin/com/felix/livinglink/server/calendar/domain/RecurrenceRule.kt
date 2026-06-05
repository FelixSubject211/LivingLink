package com.felix.livinglink.server.calendar.domain

import kotlin.time.Instant

data class RecurrenceRule(
    val frequency: Frequency,
    val interval: Int,
    val end: RecurrenceEnd,
) {
    init {
        require(interval >= 1) { "RecurrenceRule.interval must be >= 1" }
    }

    sealed interface RecurrenceEnd {
        data object Never : RecurrenceEnd

        data class Until(
            val at: Instant,
        ) : RecurrenceEnd

        data class Count(
            val occurrences: Int,
        ) : RecurrenceEnd {
            init {
                require(occurrences >= 1) { "RecurrenceEnd.Count.occurrences must be >= 1" }
            }
        }
    }

    enum class Frequency {
        Daily,
        Weekly,
        Monthly,
        Yearly,
    }
}
