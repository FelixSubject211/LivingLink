package com.felix.livinglink.server.group.application

import com.felix.livinglink.server.group.domain.ActiveMcpGroupRepository
import com.felix.livinglink.server.group.domain.Group
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.exactly
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SetActiveGroupUseCaseTest {
    private val getGroupsForUserUseCase = mock<GetGroupsForUserUseCase>()
    private val activeMcpGroupRepository = mock<ActiveMcpGroupRepository>()

    private val useCase =
        SetActiveGroupUseCase(
            getGroupsForUserUseCase = getGroupsForUserUseCase,
            activeMcpGroupRepository = activeMcpGroupRepository,
        )

    private val familie =
        Group(id = "familie", name = "Familie", memberUserIds = setOf("max", "anna"))
    private val freunde =
        Group(id = "freunde", name = "Freunde", memberUserIds = setOf("max", "tom"))

    @Test
    fun `persists and returns the group when the user is a member`() =
        runTest {
            every { getGroupsForUserUseCase("max") } returns listOf(familie, freunde)
            everySuspend { activeMcpGroupRepository.setActiveMcpGroupId("max", "freunde") } returns Unit

            val result = useCase(userId = "max", groupId = "freunde")

            assertEquals(freunde, result)
            verifySuspend(exactly(1)) { activeMcpGroupRepository.setActiveMcpGroupId("max", "freunde") }
        }

    @Test
    fun `throws and does not persist when the group is not one of the user's groups`() =
        runTest {
            every { getGroupsForUserUseCase("max") } returns listOf(familie)

            assertFailsWith<IllegalArgumentException> {
                useCase(userId = "max", groupId = "freunde")
            }

            verifySuspend(exactly(0)) { activeMcpGroupRepository.setActiveMcpGroupId("max", "freunde") }
        }
}
