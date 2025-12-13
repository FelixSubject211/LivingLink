package felix.projekt.livinglink.server.groups.application

import felix.projekt.livinglink.server.groups.domain.GetGroupsResponse
import felix.projekt.livinglink.server.groups.domain.GroupRepository
import felix.projekt.livinglink.server.groups.domain.GroupVersionCache
import felix.projekt.livinglink.server.groups.interfaces.GetUserGroupsUseCase

class GetUserGroupsDefaultUseCase(
    private val groupRepository: GroupRepository,
    private val groupVersionCache: GroupVersionCache
) : GetUserGroupsUseCase {
    override suspend fun invoke(
        userId: String,
        currentGroupVersions: Map<String, Long>
    ): GetGroupsResponse {
        val cachedVersions = groupVersionCache.getGroupVersions(userId)

        if (cachedVersions != null && cachedVersions.matches(current = currentGroupVersions)) {
            return GetGroupsResponse.NotModified
        }

        val groups = groupRepository.getGroupsForMember(userId = userId)

        val newVersions = GroupVersionCache.GroupVersions(
            groupIdsToGroupVersion = groups.mapValues { it.value.version }
        )
        groupVersionCache.setGroupVersions(userId = userId, versions = newVersions)

        return if (newVersions.matches(current = currentGroupVersions)) {
            GetGroupsResponse.NotModified
        } else {
            GetGroupsResponse.Success(groups = groups)
        }
    }

    private fun GroupVersionCache.GroupVersions.matches(current: Map<String, Long>): Boolean {
        if (groupIdsToGroupVersion.size != current.size) {
            return false
        }

        return groupIdsToGroupVersion.all { (id, version) -> current[id] == version }
    }
}