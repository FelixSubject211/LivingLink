package com.felix.livinglink.server.group.application

import com.felix.livinglink.server.group.domain.Group
import com.felix.livinglink.server.group.domain.GroupProvider
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertEquals

class GetGroupsForUserDefaultUseCaseTest {
    private val groupProvider = mock<GroupProvider>()

    private val useCase = GetGroupsForUserDefaultUseCase(groupProvider = groupProvider)

    private val familie =
        Group(id = "familie", name = "Familie", memberUserIds = setOf("max", "anna"))
    private val freunde =
        Group(id = "freunde", name = "Freunde", memberUserIds = setOf("max", "tom"))
    private val arbeit =
        Group(id = "arbeit", name = "Arbeit", memberUserIds = setOf("anna"))

    @Test
    fun `returns only groups the user is a member of, sorted by name`() {
        every { groupProvider.groupsById() } returns
            mapOf(
                "freunde" to freunde,
                "familie" to familie,
                "arbeit" to arbeit,
            )

        val result = useCase(userId = "max")

        assertEquals(listOf(familie, freunde), result)
    }

    @Test
    fun `returns empty list when the user is in no group`() {
        every { groupProvider.groupsById() } returns mapOf("arbeit" to arbeit)

        val result = useCase(userId = "max")

        assertEquals(emptyList(), result)
    }
}
