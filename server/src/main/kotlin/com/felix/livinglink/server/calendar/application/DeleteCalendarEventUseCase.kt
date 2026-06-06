package com.felix.livinglink.server.calendar.application

import com.felix.livinglink.server.calendar.domain.CalendarEventRepository
import com.felix.livinglink.server.core.domain.DeleteResult
import com.felix.livinglink.server.group.application.RequireGroupMembershipUseCase
import org.koin.core.annotation.Single

@Single
class DeleteCalendarEventUseCase(
    private val calendarEventRepository: CalendarEventRepository,
    private val requireGroupMembershipUseCase: RequireGroupMembershipUseCase,
) {
    suspend operator fun invoke(input: Input): Output {
        requireGroupMembershipUseCase(userId = input.byUserId, groupId = input.groupId)

        val existing = calendarEventRepository.findById(input.eventId)
        if (existing == null || existing.groupId != input.groupId) {
            return Output.NotFound
        }

        return when (calendarEventRepository.deleteById(input.eventId)) {
            is DeleteResult.Deleted -> Output.Deleted
            is DeleteResult.NotFound -> Output.NotFound
        }
    }

    data class Input(
        val byUserId: String,
        val groupId: String,
        val eventId: String,
    )

    sealed class Output {
        data object Deleted : Output()

        data object NotFound : Output()
    }
}
