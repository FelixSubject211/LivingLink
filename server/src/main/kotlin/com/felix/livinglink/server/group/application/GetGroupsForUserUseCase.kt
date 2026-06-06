package com.felix.livinglink.server.group.application

import com.felix.livinglink.server.group.domain.Group
import com.felix.livinglink.server.group.domain.GroupProvider
import org.koin.core.annotation.Single

interface GetGroupsForUserUseCase {
    operator fun invoke(userId: String): List<Group>
}

@Single(binds = [GetGroupsForUserUseCase::class])
class GetGroupsForUserDefaultUseCase(
    private val groupProvider: GroupProvider,
) : GetGroupsForUserUseCase {
    override operator fun invoke(userId: String): List<Group> =
        groupProvider
            .groupsById()
            .values
            .filter { group -> userId in group.memberUserIds }
            .sortedBy { it.name }
}
