package com.felix.livinglink.server.calendar.domain

import kotlin.time.Instant

data class Participant(
    val userId: String,
    val rsvpEvents: List<RsvpEvent>,
) {
    val currentStatus: RsvpStatus
        get() = rsvpEvents.lastOrNull()?.status ?: RsvpStatus.Pending

    data class RsvpEvent(
        val status: RsvpStatus,
        val at: Instant,
    )

    enum class RsvpStatus {
        Pending,
        Accepted,
        Declined,
        Maybe,
    }
}
