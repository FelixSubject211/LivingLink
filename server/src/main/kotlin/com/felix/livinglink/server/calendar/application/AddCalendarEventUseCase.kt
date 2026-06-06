package com.felix.livinglink.server.calendar.application

import com.felix.livinglink.server.calendar.domain.CalendarEvent
import com.felix.livinglink.server.calendar.domain.CalendarEventRepository
import com.felix.livinglink.server.calendar.domain.EventCategory
import com.felix.livinglink.server.calendar.domain.EventSpan
import com.felix.livinglink.server.calendar.domain.Participant
import com.felix.livinglink.server.calendar.domain.RecurrenceRule
import com.felix.livinglink.server.core.domain.TimeProvider
import com.felix.livinglink.server.core.domain.UuidGenerator
import org.koin.core.annotation.Single

@Single
class AddCalendarEventUseCase(
    private val calendarEventRepository: CalendarEventRepository,
    private val uuidGenerator: UuidGenerator,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(input: Input): CalendarEvent {
        val now = timeProvider()

        val participants =
            input.participantUserIds.map { userId ->
                Participant(
                    userId = userId,
                    rsvpEvents = emptyList(),
                )
            }

        return calendarEventRepository.create(
            CalendarEvent(
                id = uuidGenerator(),
                title = input.title,
                description = input.description,
                createdByUserId = input.byUserId,
                span = input.span,
                recurrence = input.recurrence,
                participants = participants,
                category = input.category,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    data class Input(
        val byUserId: String,
        val title: String,
        val description: String?,
        val span: EventSpan,
        val recurrence: RecurrenceRule?,
        val category: EventCategory,
        val participantUserIds: Set<String>,
    )
}
