package com.felix.livinglink.server.group.application

import com.felix.livinglink.server.group.domain.ActiveMcpGroupRepository
import com.felix.livinglink.server.group.domain.Group
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GetActiveMcpGroupUseCaseTest {
    private val getGroupsForUserUseCase = mock<GetGroupsForUserUseCase>()
    private val activeMcpGroupRepository = mock<ActiveMcpGroupRepository>()

    private val useCase =
        GetActiveMcpGroupUseCase(
            getGroupsForUserUseCase = getGroupsForUserUseCase,
            activeMcpGroupRepository = activeMcpGroupRepository,
        )

    private val familie =
        Group(id = "familie", name = "Familie", memberUserIds = setOf("max", "anna"))
    private val freunde =
        Group(id = "freunde", name = "Freunde", memberUserIds = setOf("max", "tom"))

    @Test
    fun `returns the stored group when it is still one of the user's groups`() =
        runTest {
            every { getGroupsForUserUseCase("max") } returns listOf(familie, freunde)
            everySuspend { activeMcpGroupRepository.getActiveMcpGroupId("max") } returns "freunde"

            val result = useCase(userId = "max")

            assertEquals(freunde, result)
        }

    @Test
    fun `falls back to fewest members then smallest id when nothing is stored`() =
        runTest {
            // both have 2 members -> tie -> smallest id "familie"
            every { getGroupsForUserUseCase("max") } returns listOf(familie, freunde)
            everySuspend { activeMcpGroupRepository.getActiveMcpGroupId("max") } returns null

            val result = useCase(userId = "max")

            assertEquals(familie, result)
        }

    @Test
    fun `fewest members wins over id ordering`() =
        runTest {
            val big = Group(id = "aaa", name = "Big", memberUserIds = setOf("max", "anna", "tom"))
            val small = Group(id = "zzz", name = "Small", memberUserIds = setOf("max"))

            every { getGroupsForUserUseCase("max") } returns listOf(big, small)
            everySuspend { activeMcpGroupRepository.getActiveMcpGroupId("max") } returns null

            val result = useCase(userId = "max")

            assertEquals(small, result)
        }

    @Test
    fun `falls back when the stored group is no longer one of the user's groups`() =
        runTest {
            every { getGroupsForUserUseCase("max") } returns listOf(familie)
            everySuspend { activeMcpGroupRepository.getActiveMcpGroupId("max") } returns "freunde"

            val result = useCase(userId = "max")

            assertEquals(familie, result)
        }

    @Test
    fun `returns null when the user is in no group`() =
        runTest {
            every { getGroupsForUserUseCase("max") } returns emptyList()
            everySuspend { activeMcpGroupRepository.getActiveMcpGroupId("max") } returns null

            val result = useCase(userId = "max")

            assertNull(result)
        }
}
