package felix.livinglink.groups

import felix.livinglink.event.ChangeNotifier
import felix.livinglink.group.CreateGroupRequest
import felix.livinglink.group.CreateGroupResponse
import felix.livinglink.group.CreateInviteRequest
import felix.livinglink.group.CreateInviteResponse
import felix.livinglink.group.DeleteGroupResponse
import felix.livinglink.group.GetGroupsForUserResponse
import felix.livinglink.group.UseInviteRequest
import felix.livinglink.group.UseInviteResponse

class GroupService(
    private val groupStore: GroupStore,
    private val changeNotifier: ChangeNotifier
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
            changeNotifier.markGroupChangeForUser(userId)
            return CreateGroupResponse.Success(groupId)
        } ?: return CreateGroupResponse.Error
    }

    fun deleteGroup(groupId: String, userId: String): DeleteGroupResponse {
        val allowed = groupStore.getGroupsForUser(userId).any { it.id == groupId }
        if (!allowed) return DeleteGroupResponse.NotAllowed

        val userIds = groupStore.getUserIdsInGroup(groupId)
        val success = groupStore.deleteGroup(groupId)

        if (success) {
            userIds.forEach { changeNotifier.markGroupChangeForUser(it) }
            return DeleteGroupResponse.Success
        }

        return DeleteGroupResponse.Error
    }

    fun createInviteCode(request: CreateInviteRequest, userId: String): CreateInviteResponse? {
        val code = groupStore.createInviteCode(request.groupId, userId)
        return code?.let { CreateInviteResponse(it) }
    }

    fun useInviteCode(request: UseInviteRequest, userId: String): UseInviteResponse {
        val groupId = groupStore.useInviteCode(request.code, userId)
        if (groupId != null) {
            val userIds = groupStore.getUserIdsInGroup(groupId)
            userIds.forEach { changeNotifier.markGroupChangeForUser(it) }
            return UseInviteResponse.Success
        }
        return UseInviteResponse.InvalidOrAlreadyUsed
    }
}