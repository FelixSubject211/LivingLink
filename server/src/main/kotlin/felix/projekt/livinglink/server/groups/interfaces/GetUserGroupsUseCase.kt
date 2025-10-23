package felix.projekt.livinglink.server.groups.interfaces

import felix.projekt.livinglink.server.groups.domain.GetGroupsResponse

interface GetUserGroupsUseCase {
    suspend operator fun invoke(userId: String, currentGroupVersions: Map<String, Long>): GetGroupsResponse
}