package felix.livinglink.groups

import felix.livinglink.group.CreateGroupRequest
import felix.livinglink.group.CreateGroupResponse
import felix.livinglink.group.CreateInviteRequest
import felix.livinglink.group.CreateInviteResponse
import felix.livinglink.group.DeleteGroupResponse
import felix.livinglink.group.GetGroupsForUserResponse
import felix.livinglink.group.Group
import felix.livinglink.group.UseInviteRequest
import felix.livinglink.group.UseInviteResponse

class GroupService(
    private val groupStore: GroupStore
) {

    fun getGroupsForUser(userId: String): GetGroupsForUserResponse {
        return GetGroupsForUserResponse(
            groups = groupStore.getGroupsForUser(userId)
        )
    }

    fun createGroup(request: CreateGroupRequest, userId: String): CreateGroupResponse {
        groupStore.createGroup(
            name = request.groupName,
            creatorUserId = userId
        )?.let { groupId ->
            return CreateGroupResponse.Success(groupId)
        } ?: return CreateGroupResponse.Error
    }

    fun deleteGroup(groupId: String, userId: String): DeleteGroupResponse {
        return when(groupStore.getGroupsForUser(userId).firstOrNull { it.id == groupId }) {
            is Group -> {
                if (groupStore.deleteGroup(groupId)) {
                    DeleteGroupResponse.Success
                } else {
                    DeleteGroupResponse.Error
                }
            }

            else -> {
                DeleteGroupResponse.NotAllowed
            }
        }
    }

    fun createInviteCode(request: CreateInviteRequest, userId: String): CreateInviteResponse? {
        val code = groupStore.createInviteCode(request.groupId, userId)
        return code?.let { CreateInviteResponse(it) }
    }

    fun useInviteCode(request: UseInviteRequest, userId: String): UseInviteResponse {
        val success = groupStore.useInviteCode(request.code, userId)
        return if (success) UseInviteResponse.Success else UseInviteResponse.InvalidOrAlreadyUsed
    }
}