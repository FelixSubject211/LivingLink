package felix.projekt.livinglink.server.groups.application

import felix.projekt.livinglink.server.groups.domain.GroupRepository
import felix.projekt.livinglink.server.groups.domain.GroupVersionCache
import felix.projekt.livinglink.server.groups.interfaces.RemoveUserFromGroupsService

class RemoveUserFromGroupsDefaultService(
    private val groupRepository: GroupRepository,
    private val groupVersionCache: GroupVersionCache
) : RemoveUserFromGroupsService {
    override suspend fun invoke(userId: String) {
        val allGroups = groupRepository.getGroupsForMember(userId)
        allGroups.values.forEach { group ->
            if (group.isSingleMember(userId)) {
                groupRepository.deleteGroup(group.id)
            } else {
                val result = groupRepository.updateWithOptimisticLocking(group.id) { currentGroup ->
                    GroupRepository.UpdateOperationResult.Updated(
                        newEntity = currentGroup.removeMember(userId),
                        response = Unit
                    )
                }

                result.entity?.memberIdToMember?.keys?.forEach { memberId ->
                    groupVersionCache.addOrUpdateGroupVersionIfUserExists(
                        userId = memberId,
                        groupId = group.id,
                        version = result.entity.version
                    )
                }
            }
        }
        groupVersionCache.deleteGroupVersions(userId)
    }
}