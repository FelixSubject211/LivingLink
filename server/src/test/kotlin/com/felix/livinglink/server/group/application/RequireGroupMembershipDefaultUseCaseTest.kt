package com.felix.livinglink.server.group.application

import com.felix.livinglink.server.group.domain.Group
import com.felix.livinglink.server.group.domain.GroupProvider
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertFailsWith

class RequireGroupMembershipDefaultUseCaseTest {
    private val groupProvider = mock<GroupProvider>()

    private val useCase = RequireGroupMembershipDefaultUseCase(groupProvider = groupProvider)

    private val familie =
        Group(id = "familie", name = "Familie", memberUserIds = setOf("max", "anna"))

    @Test
    fun `passes when the user is a member of the group`() {
        every { groupProvider.groupsById() } returns mapOf("familie" to familie)

        useCase(userId = "max", groupId = "familie")
    }

    @Test
    fun `throws when the user is not a member of the group`() {
        every { groupProvider.groupsById() } returns mapOf("familie" to familie)

        assertFailsWith<IllegalArgumentException> {
            useCase(userId = "tom", groupId = "familie")
        }
    }

    @Test
    fun `throws when the group does not exist`() {
        every { groupProvider.groupsById() } returns mapOf("familie" to familie)

        assertFailsWith<IllegalArgumentException> {
            useCase(userId = "max", groupId = "does-not-exist")
        }
    }
}
