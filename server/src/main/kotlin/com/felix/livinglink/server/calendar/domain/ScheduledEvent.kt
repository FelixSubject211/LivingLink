package com.felix.livinglink.server.calendar.domain

import kotlin.time.Instant

data class ScheduledEvent(
    val sourceEventId: String,
    val title: String,
    val description: String?,
    val createdByUserId: String,
    val span: EventSpan,
    val participants: List<Participant>,
    val category: EventCategory,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    val referencedUserIds: Set<String>
        get() =
            buildSet {
                add(createdByUserId)
                participants.forEach { add(it.userId) }
            }
}
