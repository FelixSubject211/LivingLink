package com.felix.livinglink.server.calendar.application

import com.felix.livinglink.server.calendar.application.GetEventCategoriesUseCase
import com.felix.livinglink.server.calendar.domain.CalendarEventRepository
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetEventCategoriesUseCaseTest {
    private val repository = mock<CalendarEventRepository>()

    private val useCase = GetEventCategoriesUseCase(calendarEventRepository = repository)

    @Test
    fun `returns sorted distinct custom labels from repository`() =
        runTest {
            everySuspend { repository.findDistinctCustomCategoryLabels() } returns listOf("Arzt", "Besuch", "Urlaub")

            val result = useCase()

            assertEquals(listOf("Arzt", "Besuch", "Urlaub"), result.knownCustomLabels)
        }

    @Test
    fun `returns empty list when repository returns no labels`() =
        runTest {
            everySuspend { repository.findDistinctCustomCategoryLabels() } returns emptyList()

            val result = useCase()

            assertEquals(emptyList(), result.knownCustomLabels)
        }
}
