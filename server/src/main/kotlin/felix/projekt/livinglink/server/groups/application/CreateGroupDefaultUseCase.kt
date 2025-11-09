package felix.projekt.livinglink.server.groups.application

import felix.projekt.livinglink.server.groups.domain.Group
import felix.projekt.livinglink.server.groups.domain.GroupRepository
import felix.projekt.livinglink.server.groups.domain.GroupVersionCache
import felix.projekt.livinglink.server.groups.interfaces.CreateGroupUseCase

class CreateGroupDefaultUseCase(
    private val groupRepository: GroupRepository,
    private val groupVersionCache: GroupVersionCache
) : CreateGroupUseCase {
    override suspend fun invoke(
        userId: String,
        username: String,
        groupName: String
    ): Group {
        val newGroup = groupRepository.createGroup(groupName)
        val result = groupRepository.updateWithOptimisticLocking(newGroup.id) { group ->
            GroupRepository.UpdateOperationResult.Updated(
                newEntity = group.addMember(userId, username),
                response = Unit
            )
        }

        result.entity?.memberIdToMember?.values?.forEach { member ->
            groupVersionCache.addOrUpdateGroupVersionIfUserExists(
                userId = member.id,
                groupId = newGroup.id,
                version = result.entity.version
            )
        }

        return result.entity ?: throw IllegalStateException()
    }
}