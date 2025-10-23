package felix.projekt.livinglink.composeApp.groups.infrastructure

import felix.projekt.livinglink.composeApp.core.domain.NetworkError
import felix.projekt.livinglink.composeApp.core.domain.Result
import felix.projekt.livinglink.composeApp.core.domain.map
import felix.projekt.livinglink.composeApp.core.infrastructure.post
import felix.projekt.livinglink.composeApp.groups.domain.CreateGroupResponse
import felix.projekt.livinglink.composeApp.groups.domain.GetGroupsResponse
import felix.projekt.livinglink.composeApp.groups.domain.Group
import felix.projekt.livinglink.composeApp.groups.domain.GroupsNetworkDataSource
import felix.projekt.livinglink.shared.groups.requestModel.GroupRequest
import felix.projekt.livinglink.shared.groups.requestModel.GroupResponse
import io.ktor.client.HttpClient

class GroupsNetworkDefaultDataSource(
    private val httpClient: HttpClient
) : GroupsNetworkDataSource {
    override suspend fun getGroups(
        currentGroupVersions: Map<String, Long>
    ): Result<GetGroupsResponse, NetworkError> {
        return httpClient.post<GroupRequest.GetGroups, GroupResponse.GetGroups>(
            urlString = "groups",
            request = GroupRequest.GetGroups(currentGroupVersions = currentGroupVersions)
        ).map { response ->
            when (response) {
                is GroupResponse.GetGroups.Success -> {
                    GetGroupsResponse.Success(
                        groups = response.groups.mapValues { it.value.toDomain() },
                        nextPollAfterMillis = response.nextPollAfterMillis
                    )
                }

                is GroupResponse.GetGroups.NotModified -> {
                    GetGroupsResponse.NotModified(
                        nextPollAfterMillis = response.nextPollAfterMillis
                    )
                }
            }
        }
    }

    override suspend fun createGroup(groupName: String): Result<CreateGroupResponse, NetworkError> {
        return httpClient.post<GroupRequest.CreateGroup, GroupResponse.CreateGroup>(
            urlString = "groups/create",
            request = GroupRequest.CreateGroup(groupName = groupName)
        ).map { response ->
            when (response) {
                is GroupResponse.CreateGroup.Success -> {
                    CreateGroupResponse.Success(response.group.toDomain())
                }
            }
        }
    }

    private fun GroupResponse.Group.toDomain() = Group(
        id = this.id,
        name = this.name,
        memberIdToMember = this.memberIdToMember.mapValues { member ->
            Group.Member(
                id = member.value.id,
                username = member.value.username
            )
        },
        version = this.version
    )
}