package com.felix.livinglink.server.calendar.application

import com.felix.livinglink.server.calendar.domain.CalendarEvent
import com.felix.livinglink.server.calendar.domain.CalendarEventRepository
import com.felix.livinglink.server.calendar.domain.EventCategory
import com.felix.livinglink.server.calendar.domain.EventSpan
import com.felix.livinglink.server.core.domain.DeleteResult
import com.felix.livinglink.server.group.application.RequireGroupMembershipUseCase
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.exactly
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

class DeleteCalendarEventUseCaseTest {
    private val calendarEventRepository = mock<CalendarEventRepository>()
    private val requireGroupMembershipUseCase = mock<RequireGroupMembershipUseCase>()

    private val useCase =
        DeleteCalendarEventUseCase(
            calendarEventRepository = calendarEventRepository,
            requireGroupMembershipUseCase = requireGroupMembershipUseCase,
        )

    private val t0 = Instant.fromEpochSeconds(1_700_000_000)

    private fun event(id: String, groupId: String): CalendarEvent =
        CalendarEvent(
            id = id,
            groupId = groupId,
            title = "event-$id",
            description = null,
            createdByUserId = "creator",
            span = EventSpan.Timed(start = t0, end = t0 + 1.hours),
            recurrence = null,
            participants = emptyList(),
            category = EventCategory.None,
            createdAt = t0,
            updatedAt = t0,
        )

    @Test
    fun `returns Deleted when the event belongs to the group and is deleted`() =
        runTest {
            every { requireGroupMembershipUseCase("user-1", "group-1") } returns Unit
            everySuspend { calendarEventRepository.findById("event-1") } returns event("event-1", "group-1")
            everySuspend { calendarEventRepository.deleteById("event-1") } returns DeleteResult.Deleted

            val result =
                useCase(
                    DeleteCalendarEventUseCase.Input(
                        byUserId = "user-1",
                        groupId = "group-1",
                        eventId = "event-1",
                    ),
                )

            assertEquals(DeleteCalendarEventUseCase.Output.Deleted, result)
        }

    @Test
    fun `returns NotFound when the event does not exist`() =
        runTest {
            every { requireGroupMembershipUseCase("user-1", "group-1") } returns Unit
            everySuspend { calendarEventRepository.findById("event-99") } returns null

            val result =
                useCase(
                    DeleteCalendarEventUseCase.Input(
                        byUserId = "user-1",
                        groupId = "group-1",
                        eventId = "event-99",
                    ),
                )

            assertEquals(DeleteCalendarEventUseCase.Output.NotFound, result)
            verifySuspend(exactly(0)) { calendarEventRepository.deleteById(any()) }
        }

    @Test
    fun `returns NotFound and never deletes when the event belongs to another group`() =
        runTest {
            every { requireGroupMembershipUseCase("user-1", "group-1") } returns Unit
            everySuspend { calendarEventRepository.findById("event-1") } returns event("event-1", "other-group")

            val result =
                useCase(
                    DeleteCalendarEventUseCase.Input(
                        byUserId = "user-1",
                        groupId = "group-1",
                        eventId = "event-1",
                    ),
                )

            assertEquals(DeleteCalendarEventUseCase.Output.NotFound, result)
            verifySuspend(exactly(0)) { calendarEventRepository.deleteById(any()) }
        }

    @Test
    fun `throws and deletes nothing when the user is not a member of the group`() =
        runTest {
            every { requireGroupMembershipUseCase("user-1", "group-1") } throws
                IllegalArgumentException("User 'user-1' is not a member of group 'group-1'.")

            assertFailsWith<IllegalArgumentException> {
                useCase(
                    DeleteCalendarEventUseCase.Input(
                        byUserId = "user-1",
                        groupId = "group-1",
                        eventId = "event-1",
                    ),
                )
            }

            verifySuspend(exactly(0)) { calendarEventRepository.findById(any()) }
            verifySuspend(exactly(0)) { calendarEventRepository.deleteById(any()) }
        }
}
