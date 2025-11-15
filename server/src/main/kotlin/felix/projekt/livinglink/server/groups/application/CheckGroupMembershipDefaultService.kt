package felix.projekt.livinglink.server.groups.application

import felix.projekt.livinglink.server.groups.domain.GroupRepository
import felix.projekt.livinglink.server.groups.domain.GroupVersionCache
import felix.projekt.livinglink.server.groups.interfaces.CheckGroupMembershipService

class CheckGroupMembershipDefaultService(
    private val groupRepository: GroupRepository,
    private val groupVersionCache: GroupVersionCache
) : CheckGroupMembershipService {
    override suspend operator fun invoke(userId: String, groupId: String): Boolean {
        val cached = groupVersionCache.getGroupVersions(userId)
        if (cached != null) {
            if (cached.groupIdsToGroupVersion.containsKey(groupId)) {
                return true
            }
        }
        val group = groupRepository.getGroupById(groupId) ?: return false
        return group.memberIdToMember.containsKey(userId)
    }
}