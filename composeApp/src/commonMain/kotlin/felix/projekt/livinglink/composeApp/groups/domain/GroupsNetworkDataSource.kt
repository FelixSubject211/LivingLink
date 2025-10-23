package felix.projekt.livinglink.composeApp.groups.domain

import felix.projekt.livinglink.composeApp.core.domain.NetworkError
import felix.projekt.livinglink.composeApp.core.domain.Result

interface GroupsNetworkDataSource {
    suspend fun getGroups(currentGroupVersions: Map<String, Long>): Result<GetGroupsResponse, NetworkError>
    suspend fun createGroup(groupName: String): Result<CreateGroupResponse, NetworkError>
}