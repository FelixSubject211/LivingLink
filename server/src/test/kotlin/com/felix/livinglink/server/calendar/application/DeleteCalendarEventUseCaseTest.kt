package com.felix.livinglink.server.calendar.application

import com.felix.livinglink.server.calendar.application.DeleteCalendarEventUseCase
import com.felix.livinglink.server.calendar.domain.CalendarEventRepository
import com.felix.livinglink.server.core.domain.DeleteResult
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DeleteCalendarEventUseCaseTest {
    private val calendarEventRepository = mock<CalendarEventRepository>()

    private val useCase =
        DeleteCalendarEventUseCase(
            calendarEventRepository = calendarEventRepository,
        )

    @Test
    fun `returns Deleted when the repository deletes the event`() =
        runTest {
            everySuspend { calendarEventRepository.deleteById("event-1") } returns DeleteResult.Deleted

            val result = useCase(eventId = "event-1")

            assertEquals(DeleteCalendarEventUseCase.Output.Deleted, result)
        }

    @Test
    fun `returns NotFound when the repository reports the event is missing`() =
        runTest {
            everySuspend { calendarEventRepository.deleteById("event-99") } returns DeleteResult.NotFound

            val result = useCase(eventId = "event-99")

            assertEquals(DeleteCalendarEventUseCase.Output.NotFound, result)
        }
}
