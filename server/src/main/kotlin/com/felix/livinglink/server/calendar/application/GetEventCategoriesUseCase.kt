package com.felix.livinglink.server.calendar.application

import com.felix.livinglink.server.calendar.domain.CalendarEventRepository
import com.felix.livinglink.server.group.application.RequireGroupMembershipUseCase
import org.koin.core.annotation.Single

@Single
class GetEventCategoriesUseCase(
    private val calendarEventRepository: CalendarEventRepository,
    private val requireGroupMembershipUseCase: RequireGroupMembershipUseCase,
) {
    suspend operator fun invoke(input: Input): List<String> {
        requireGroupMembershipUseCase(userId = input.byUserId, groupId = input.groupId)
        return calendarEventRepository.findDistinctCustomCategoryLabels(input.groupId)
    }

    data class Input(
        val byUserId: String,
        val groupId: String,
    )
}
