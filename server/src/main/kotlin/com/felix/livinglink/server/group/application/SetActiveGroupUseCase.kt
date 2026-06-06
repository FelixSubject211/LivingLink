package com.felix.livinglink.server.group.application

import com.felix.livinglink.server.group.domain.ActiveMcpGroupRepository
import com.felix.livinglink.server.group.domain.Group
import org.koin.core.annotation.Single

@Single
class SetActiveGroupUseCase(
    private val getGroupsForUserUseCase: GetGroupsForUserUseCase,
    private val activeMcpGroupRepository: ActiveMcpGroupRepository,
) {
    suspend operator fun invoke(userId: String, groupId: String): Group {
        val group =
            getGroupsForUserUseCase(userId).firstOrNull { it.id == groupId }
                ?: throw IllegalArgumentException(
                    "Group '$groupId' does not exist or user '$userId' is not a member.",
                )

        activeMcpGroupRepository.setActiveMcpGroupId(userId = userId, groupId = group.id)
        return group
    }
}
