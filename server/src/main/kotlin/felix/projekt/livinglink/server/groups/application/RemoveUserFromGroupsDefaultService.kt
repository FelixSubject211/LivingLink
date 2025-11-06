package felix.projekt.livinglink.server.groups.application

import felix.projekt.livinglink.server.groups.domain.GroupRepository
import felix.projekt.livinglink.server.groups.domain.GroupVersionCache
import felix.projekt.livinglink.server.groups.interfaces.RemoveUserFromGroupsService

class RemoveUserFromGroupsDefaultService(
    private val groupRepository: GroupRepository,
    private val groupVersionCache: GroupVersionCache
) : RemoveUserFromGroupsService {
    override suspend fun invoke(userId: String) {
        val allGroups = groupRepository.getGroupsForMember(userId = userId)
        allGroups.forEach { (_, group) ->
            if (group.isSingleMember(userId = userId)) {
                groupRepository.deleteGroup(groupId = group.id)
            } else {
                val updatedGroup = groupRepository.updateWithOptimisticLocking(
                    groupId = group.id
                ) { currentGroup ->
                    currentGroup.removeMember(userId = userId)
                } ?: throw IllegalStateException()

                updatedGroup.memberIdToMember.keys.forEach { memberId ->
                    groupVersionCache.addOrUpdateGroupVersionIfUserExists(
                        userId = memberId,
                        groupId = updatedGroup.id,
                        version = updatedGroup.version
                    )
                }
            }
        }
        groupVersionCache.deleteGroupVersions(userId = userId)
    }
}