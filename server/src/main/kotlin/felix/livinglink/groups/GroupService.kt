package felix.livinglink.groups

import felix.livinglink.event.ChangeNotifier
import felix.livinglink.group.CreateGroupRequest
import felix.livinglink.group.CreateGroupResponse
import felix.livinglink.group.CreateInviteRequest
import felix.livinglink.group.CreateInviteResponse
import felix.livinglink.group.DeleteGroupResponse
import felix.livinglink.group.GetGroupsForUserResponse
import felix.livinglink.group.LeaveGroupResponse
import felix.livinglink.group.MakeUserAdminRequest
import felix.livinglink.group.MakeUserAdminResponse
import felix.livinglink.group.RemoveUserFromGroupRequest
import felix.livinglink.group.RemoveUserFromGroupResponse
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
        if (!isAdmin(userId = userId, groupId = groupId)) return DeleteGroupResponse.NotAllowed

        val userIds = groupStore.getUserIdsInGroup(groupId)
        val success = groupStore.deleteGroup(groupId)

        if (success) {
            userIds.forEach { changeNotifier.markGroupChangeForUser(it) }
            return DeleteGroupResponse.Success
        }

        return DeleteGroupResponse.Error
    }

    fun leaveGroup(groupId: String, userId: String): LeaveGroupResponse {
        if (!groupStore.isUserIdInGroup(userId = userId, groupId = groupId)) {
            return LeaveGroupResponse.NotAllowed
        }

        val admins = groupStore.getAdminUserIdsInGroup(groupId)
        if (admins.size == 1 && admins.first() == userId) {
            return LeaveGroupResponse.LastAdminCannotLeave
        }

        val userIds = groupStore.getUserIdsInGroup(groupId)
        val success = groupStore.removeUserFromGroup(userId = userId, groupId = groupId)

        return if (success) {
            userIds.forEach { changeNotifier.markGroupChangeForUser(it) }
            LeaveGroupResponse.Success
        } else {
            LeaveGroupResponse.Error
        }
    }

    fun createInviteCode(request: CreateInviteRequest, userId: String): CreateInviteResponse? {
        if (!isAdmin(userId = userId, groupId = request.groupId)) {
            return CreateInviteResponse.Error
        }
        val code = groupStore.createInviteCode(request.groupId, userId)
        return code?.let { CreateInviteResponse.Success(it) }
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

    fun removeUserFromGroup(
        request: RemoveUserFromGroupRequest,
        userId: String
    ): RemoveUserFromGroupResponse {
        if (!isAdmin(userId = userId, groupId = request.groupId)) {
            return RemoveUserFromGroupResponse.NotAllowed
        }

        val memberIds = groupStore.getUserIdsInGroup(request.groupId)
        if (!memberIds.contains(request.userId)) {
            return RemoveUserFromGroupResponse.NotAllowed
        }

        val success = groupStore.removeUserFromGroup(
            userId = request.userId,
            groupId = request.groupId
        )

        return if (success) {
            memberIds.forEach { changeNotifier.markGroupChangeForUser(it) }
            RemoveUserFromGroupResponse.Success
        } else {
            RemoveUserFromGroupResponse.Error
        }
    }

    fun makeUserAdmin(
        request: MakeUserAdminRequest,
        userId: String
    ): MakeUserAdminResponse {
        if (!isAdmin(userId = userId, groupId = request.groupId)) {
            return MakeUserAdminResponse.NotAllowed
        }

        val memberIds = groupStore.getUserIdsInGroup(request.groupId)
        if (!memberIds.contains(request.userId)) {
            return MakeUserAdminResponse.NotAllowed
        }

        val success = groupStore.makeUserAdmin(
            userId = request.userId,
            groupId = request.groupId
        )

        return if (success) {
            memberIds.forEach { changeNotifier.markGroupChangeForUser(it) }
            MakeUserAdminResponse.Success
        } else {
            MakeUserAdminResponse.Error
        }
    }

    private fun isAdmin(userId: String, groupId: String): Boolean {
        return groupStore
            .getAdminUserIdsInGroup(groupId)
            .contains(userId)
    }
}