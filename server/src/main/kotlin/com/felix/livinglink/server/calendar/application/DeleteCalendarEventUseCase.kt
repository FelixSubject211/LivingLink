package com.felix.livinglink.server.calendar.application

import com.felix.livinglink.server.calendar.domain.CalendarEventRepository
import com.felix.livinglink.server.core.domain.DeleteResult
import org.koin.core.annotation.Single

@Single
class DeleteCalendarEventUseCase(
    private val calendarEventRepository: CalendarEventRepository,
) {
    suspend operator fun invoke(input: Input): Output {
        val result = calendarEventRepository.deleteById(input.eventId)

        return when (result) {
            is DeleteResult.Deleted -> Output.Deleted
            is DeleteResult.NotFound -> Output.NotFound
        }
    }

    data class Input(
        val eventId: String,
    )

    sealed class Output {
        data object Deleted : Output()

        data object NotFound : Output()
    }
}
