package com.felix.livinglink.server.calendar.application

import com.felix.livinglink.server.calendar.domain.CalendarEventRepository
import org.koin.core.annotation.Single

@Single
class GetEventCategoriesUseCase(
    private val calendarEventRepository: CalendarEventRepository,
) {
    suspend operator fun invoke(): List<String> = calendarEventRepository.findDistinctCustomCategoryLabels()
}
