package felix.livinglink.groups.network

import felix.livinglink.common.model.LivingLinkResult
import felix.livinglink.common.network.NetworkError
import felix.livinglink.common.network.delete
import felix.livinglink.common.network.get
import felix.livinglink.common.network.post
import felix.livinglink.group.CreateGroupRequest
import felix.livinglink.group.CreateGroupResponse
import felix.livinglink.group.CreateInviteRequest
import felix.livinglink.group.CreateInviteResponse
import felix.livinglink.group.DeleteGroupResponse
import felix.livinglink.group.GetGroupsForUserResponse
import felix.livinglink.group.LeaveGroupRequest
import felix.livinglink.group.LeaveGroupResponse
import felix.livinglink.group.MakeUserAdminRequest
import felix.livinglink.group.MakeUserAdminResponse
import felix.livinglink.group.RemoveUserFromGroupRequest
import felix.livinglink.group.RemoveUserFromGroupResponse
import felix.livinglink.group.UseInviteRequest
import felix.livinglink.group.UseInviteResponse
import io.ktor.client.HttpClient

interface GroupsNetworkDataSource {
    suspend fun getGroupsForUser(): LivingLinkResult<GetGroupsForUserResponse, NetworkError>

    suspend fun createGroup(
        request: CreateGroupRequest
    ): LivingLinkResult<CreateGroupResponse, NetworkError>

    suspend fun deleteGroup(
        groupId: String
    ): LivingLinkResult<DeleteGroupResponse, NetworkError>

    suspend fun leaveGroup(
        request: LeaveGroupRequest
    ): LivingLinkResult<LeaveGroupResponse, NetworkError>

    suspend fun createInvite(
        request: CreateInviteRequest
    ): LivingLinkResult<CreateInviteResponse, NetworkError>

    suspend fun useInvite(
        request: UseInviteRequest
    ): LivingLinkResult<UseInviteResponse, NetworkError>

    suspend fun removeUserFromGroup(
        request: RemoveUserFromGroupRequest
    ): LivingLinkResult<RemoveUserFromGroupResponse, NetworkError>

    suspend fun makeUserAdmin(
        request: MakeUserAdminRequest
    ): LivingLinkResult<MakeUserAdminResponse, NetworkError>
}

class GroupNetworkDefaultDataSource(
    private val authenticatedHttpClient: HttpClient,
) : GroupsNetworkDataSource {

    override suspend fun getGroupsForUser(): LivingLinkResult<GetGroupsForUserResponse, NetworkError> {
        return authenticatedHttpClient.get("groups/get")
    }

    override suspend fun createGroup(
        request: CreateGroupRequest
    ): LivingLinkResult<CreateGroupResponse, NetworkError> {
        return authenticatedHttpClient.post(
            urlString = "groups/create",
            request = request
        )
    }

    override suspend fun deleteGroup(
        groupId: String
    ): LivingLinkResult<DeleteGroupResponse, NetworkError> {
        return authenticatedHttpClient.delete(urlString = "groups/$groupId")
    }

    override suspend fun leaveGroup(
        request: LeaveGroupRequest
    ): LivingLinkResult<LeaveGroupResponse, NetworkError> {
        return authenticatedHttpClient.post(
            urlString = "groups/leave",
            request = request
        )
    }

    override suspend fun createInvite(
        request: CreateInviteRequest
    ): LivingLinkResult<CreateInviteResponse, NetworkError> {
        return authenticatedHttpClient.post(
            urlString = "groups/invite/create",
            request = request
        )
    }

    override suspend fun useInvite(
        request: UseInviteRequest
    ): LivingLinkResult<UseInviteResponse, NetworkError> {
        return authenticatedHttpClient.post(
            urlString = "groups/invite/use",
            request = request
        )
    }

    override suspend fun removeUserFromGroup(
        request: RemoveUserFromGroupRequest
    ): LivingLinkResult<RemoveUserFromGroupResponse, NetworkError> {
        return authenticatedHttpClient.post(
            urlString = "groups/member/remove",
            request = request
        )
    }

    override suspend fun makeUserAdmin(
        request: MakeUserAdminRequest
    ): LivingLinkResult<MakeUserAdminResponse, NetworkError> {
        return authenticatedHttpClient.post(
            urlString = "groups/member/make-admin",
            request = request
        )
    }
}