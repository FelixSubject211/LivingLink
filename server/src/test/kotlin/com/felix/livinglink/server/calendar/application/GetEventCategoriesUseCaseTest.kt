package com.felix.livinglink.server.calendar.application

import com.felix.livinglink.server.calendar.domain.CalendarEventRepository
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

class GetEventCategoriesUseCaseTest {
    private val repository = mock<CalendarEventRepository>()
    private val requireGroupMembershipUseCase = mock<RequireGroupMembershipUseCase>()

    private val useCase =
        GetEventCategoriesUseCase(
            calendarEventRepository = repository,
            requireGroupMembershipUseCase = requireGroupMembershipUseCase,
        )

    @Test
    fun `returns the group-scoped labels from the repository`() =
        runTest {
            every { requireGroupMembershipUseCase("user-1", "group-1") } returns Unit
            everySuspend { repository.findDistinctCustomCategoryLabels("group-1") } returns
                listOf("Arzt", "Besuch", "Urlaub")

            val result =
                useCase(
                    GetEventCategoriesUseCase.Input(
                        byUserId = "user-1",
                        groupId = "group-1",
                    ),
                )

            assertEquals(listOf("Arzt", "Besuch", "Urlaub"), result)
            verifySuspend(exactly(1)) { repository.findDistinctCustomCategoryLabels("group-1") }
        }

    @Test
    fun `returns empty list when the repository returns no labels`() =
        runTest {
            every { requireGroupMembershipUseCase("user-1", "group-1") } returns Unit
            everySuspend { repository.findDistinctCustomCategoryLabels("group-1") } returns emptyList()

            val result =
                useCase(
                    GetEventCategoriesUseCase.Input(
                        byUserId = "user-1",
                        groupId = "group-1",
                    ),
                )

            assertEquals(emptyList(), result)
        }

    @Test
    fun `throws and queries nothing when the user is not a member of the group`() =
        runTest {
            every { requireGroupMembershipUseCase("user-1", "group-1") } throws
                IllegalArgumentException("User 'user-1' is not a member of group 'group-1'.")

            assertFailsWith<IllegalArgumentException> {
                useCase(
                    GetEventCategoriesUseCase.Input(
                        byUserId = "user-1",
                        groupId = "group-1",
                    ),
                )
            }

            verifySuspend(exactly(0)) { repository.findDistinctCustomCategoryLabels(any()) }
        }
}
