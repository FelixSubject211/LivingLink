package com.felix.livinglink.server.group.application

import com.felix.livinglink.server.group.domain.ActiveMcpGroupRepository
import com.felix.livinglink.server.group.domain.Group
import org.koin.core.annotation.Single

@Single
class GetActiveMcpGroupUseCase(
    private val getGroupsForUserUseCase: GetGroupsForUserUseCase,
    private val activeMcpGroupRepository: ActiveMcpGroupRepository,
) {
    suspend operator fun invoke(userId: String): Group? {
        val groups = getGroupsForUserUseCase(userId)
        if (groups.isEmpty()) return null

        val stored = activeMcpGroupRepository.getActiveMcpGroupId(userId)
        return groups.firstOrNull { it.id == stored }
            ?: groups.minWith(
                compareBy<Group> { it.memberUserIds.size }.thenBy { it.id },
            )
    }
}
