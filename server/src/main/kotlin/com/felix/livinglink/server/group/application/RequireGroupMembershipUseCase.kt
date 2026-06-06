package com.felix.livinglink.server.group.application

import com.felix.livinglink.server.group.domain.GroupProvider
import org.koin.core.annotation.Single

interface RequireGroupMembershipUseCase {
    operator fun invoke(userId: String, groupId: String)
}

@Single(binds = [RequireGroupMembershipUseCase::class])
class RequireGroupMembershipDefaultUseCase(
    private val groupProvider: GroupProvider,
) : RequireGroupMembershipUseCase {
    override operator fun invoke(userId: String, groupId: String) {
        val group = groupProvider.groupsById()[groupId]
        require(group != null && userId in group.memberUserIds) {
            "User '$userId' is not a member of group '$groupId'."
        }
    }
}
