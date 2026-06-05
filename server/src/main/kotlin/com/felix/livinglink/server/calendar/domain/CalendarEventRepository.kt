package com.felix.livinglink.server.calendar.domain

import com.felix.livinglink.server.core.domain.CrudRepository
import kotlinx.coroutines.flow.Flow

interface CalendarEventRepository : CrudRepository<CalendarEvent> {
    suspend fun find(query: CalendarEventQuery): Flow<CalendarEvent>

    suspend fun findDistinctCustomCategoryLabels(): List<String>
}
