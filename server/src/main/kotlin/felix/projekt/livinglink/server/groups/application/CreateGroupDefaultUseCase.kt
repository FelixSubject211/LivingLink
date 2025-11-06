package felix.projekt.livinglink.server.groups.application

import felix.projekt.livinglink.server.groups.domain.Group
import felix.projekt.livinglink.server.groups.domain.GroupRepository
import felix.projekt.livinglink.server.groups.domain.GroupVersionCache
import felix.projekt.livinglink.server.groups.interfaces.CreateGroupUseCase

class CreateGroupDefaultUseCase(
    private val groupRepository: GroupRepository,
    private val groupVersionCache: GroupVersionCache
) : CreateGroupUseCase {

    override suspend fun invoke(userId: String, username: String, groupName: String): Group {
        val newGroup = groupRepository.createGroup(groupName = groupName)

        val updatedGroup = groupRepository.updateWithOptimisticLocking(groupId = newGroup.id) { group ->
            group.addMember(userId = userId, username = username)
        } ?: throw IllegalStateException()

        groupVersionCache.addOrUpdateGroupVersionIfUserExists(
            userId = userId,
            groupId = updatedGroup.id,
            version = updatedGroup.version,
        )

        return updatedGroup
    }
}