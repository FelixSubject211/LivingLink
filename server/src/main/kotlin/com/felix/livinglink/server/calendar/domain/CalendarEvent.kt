package com.felix.livinglink.server.calendar.domain

import kotlin.time.Instant

data class CalendarEvent(
    val id: String,
    val groupId: String,
    val title: String,
    val description: String?,
    val createdByUserId: String,
    val span: EventSpan,
    val recurrence: RecurrenceRule?,
    val participants: List<Participant>,
    val category: EventCategory,
    val createdAt: Instant,
    val updatedAt: Instant,
    val version: Long = 0,
)
