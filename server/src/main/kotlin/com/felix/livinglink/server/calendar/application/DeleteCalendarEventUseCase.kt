package com.felix.livinglink.server.calendar.application

import com.felix.livinglink.server.calendar.domain.CalendarEventRepository
import com.felix.livinglink.server.core.domain.DeleteResult
import org.koin.core.annotation.Single

@Single
class DeleteCalendarEventUseCase(
    private val calendarEventRepository: CalendarEventRepository,
) {
    suspend operator fun invoke(eventId: String): Output {
        val result = calendarEventRepository.deleteById(eventId)

        return when (result) {
            is DeleteResult.Deleted -> Output.Deleted
            is DeleteResult.NotFound -> Output.NotFound
        }
    }

    sealed class Output {
        data object Deleted : Output()

        data object NotFound : Output()
    }
}
