package felix.projekt.livinglink.composeApp.groups.infrastructure

import felix.projekt.livinglink.composeApp.core.domain.NetworkError
import felix.projekt.livinglink.composeApp.core.domain.Result
import felix.projekt.livinglink.composeApp.core.domain.map
import felix.projekt.livinglink.composeApp.core.infrastructure.post
import felix.projekt.livinglink.composeApp.groups.domain.CreateGroupResponse
import felix.projekt.livinglink.composeApp.groups.domain.CreateGroupResponse.Success
import felix.projekt.livinglink.composeApp.groups.domain.CreateInviteCodeResponse
import felix.projekt.livinglink.composeApp.groups.domain.DeleteInviteCodeResponse
import felix.projekt.livinglink.composeApp.groups.domain.GetGroupsResponse
import felix.projekt.livinglink.composeApp.groups.domain.Group
import felix.projekt.livinglink.composeApp.groups.domain.GroupsNetworkDataSource
import felix.projekt.livinglink.composeApp.groups.domain.JoinGroupResponse
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
                    Success(response.group.toDomain())
                }
            }
        }
    }

    override suspend fun createInviteCode(
        groupId: String,
        inviteCodeName: String
    ): Result<CreateInviteCodeResponse, NetworkError> {
        return httpClient.post<GroupRequest.CreateInviteCode, GroupResponse.CreateInviteCode>(
            urlString = "groups/inviteCode/create",
            request = GroupRequest.CreateInviteCode(
                groupId = groupId,
                inviteCodeName = inviteCodeName
            )
        ).map { response ->
            when (response) {
                is GroupResponse.CreateInviteCode.Success -> {
                    CreateInviteCodeResponse.Success(key = response.key)
                }
            }
        }
    }

    override suspend fun deleteInviteCode(
        groupId: String,
        inviteCodeId: String
    ): Result<DeleteInviteCodeResponse, NetworkError> {
        return httpClient.post<GroupRequest.DeleteInviteCode, GroupResponse.DeleteInviteCode>(
            urlString = "groups/inviteCode/delete",
            request = GroupRequest.DeleteInviteCode(
                groupId = groupId,
                inviteCodeId = inviteCodeId
            )
        ).map { response ->
            when (response) {
                is GroupResponse.DeleteInviteCode.Success -> {
                    DeleteInviteCodeResponse.Success
                }
            }
        }
    }

    override suspend fun joinGroup(inviteCodeKey: String): Result<JoinGroupResponse, NetworkError> {
        return httpClient.post<GroupRequest.JoinGroup, GroupResponse.JoinGroup>(
            urlString = "groups/inviteCode/join",
            request = GroupRequest.JoinGroup(inviteCodeKey = inviteCodeKey)
        ).map { response ->
            when (response) {
                is GroupResponse.JoinGroup.Success -> {
                    JoinGroupResponse.Success(response.group.toDomain())
                }

                is GroupResponse.JoinGroup.InviteCodeNotFound -> {
                    JoinGroupResponse.InviteCodeNotFound
                }

                is GroupResponse.JoinGroup.AlreadyMember -> {
                    JoinGroupResponse.AlreadyMember
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
        inviteCodes = this.inviteCodes.map { inviteCode ->
            Group.InviteCode(
                id = inviteCode.id,
                name = inviteCode.name,
                creatorId = inviteCode.creatorId,
                usages = inviteCode.usages
            )
        },
        version = this.version,
    )
}