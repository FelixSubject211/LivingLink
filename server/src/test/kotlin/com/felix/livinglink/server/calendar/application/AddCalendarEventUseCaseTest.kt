package com.felix.livinglink.server.calendar.application

import com.felix.livinglink.server.calendar.application.AddCalendarEventUseCase
import com.felix.livinglink.server.calendar.domain.CalendarEvent
import com.felix.livinglink.server.calendar.domain.CalendarEventRepository
import com.felix.livinglink.server.calendar.domain.EventCategory
import com.felix.livinglink.server.calendar.domain.EventSpan
import com.felix.livinglink.server.calendar.domain.Participant
import com.felix.livinglink.server.calendar.domain.RecurrenceRule
import com.felix.livinglink.server.core.domain.TimeProvider
import com.felix.livinglink.server.core.domain.UuidGenerator
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

class AddCalendarEventUseCaseTest {
    private val repository = mock<CalendarEventRepository>()
    private val uuidGenerator = mock<UuidGenerator>()
    private val timeProvider = mock<TimeProvider>()

    private val useCase =
        AddCalendarEventUseCase(
            calendarEventRepository = repository,
            uuidGenerator = uuidGenerator,
            timeProvider = timeProvider,
        )

    @Test
    fun `builds event from input and persists it via the repository`() =
        runTest {
            val now = Clock.System.now()
            val span = EventSpan.Timed(start = now + 1.hours, end = now + 2.hours)
            val recurrence =
                RecurrenceRule(
                    frequency = RecurrenceRule.Frequency.Weekly,
                    interval = 1,
                    end = RecurrenceRule.RecurrenceEnd.Never,
                )

            every { uuidGenerator() } returns "id-1"
            every { timeProvider() } returns now
            everySuspend { repository.create(any()) } calls { (event: CalendarEvent) -> event }

            val participantUserIds = setOf("user-a", "user-b")

            val result =
                useCase(
                    AddCalendarEventUseCase.Input(
                        byUserId = "user-1",
                        title = "Weekly sync",
                        description = "team standup",
                        span = span,
                        recurrence = recurrence,
                        category = EventCategory.Custom(label = "Work"),
                        participantUserIds = participantUserIds,
                    ),
                )

            val expected =
                CalendarEvent(
                    id = "id-1",
                    title = "Weekly sync",
                    description = "team standup",
                    createdByUserId = "user-1",
                    span = span,
                    recurrence = recurrence,
                    participants =
                        participantUserIds.map { userId ->
                            Participant(userId = userId, rsvpEvents = emptyList())
                        },
                    category = EventCategory.Custom(label = "Work"),
                    createdAt = now,
                    updatedAt = now,
                )

            assertEquals(expected, result)
        }
}
