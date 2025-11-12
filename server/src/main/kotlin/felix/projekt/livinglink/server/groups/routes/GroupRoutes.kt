package felix.projekt.livinglink.server.groups.routes

import felix.projekt.livinglink.server.core.routes.userId
import felix.projekt.livinglink.server.core.routes.username
import felix.projekt.livinglink.server.groups.config.GroupsConfig
import felix.projekt.livinglink.server.groups.domain.CreateInviteCodeResponse
import felix.projekt.livinglink.server.groups.domain.DeleteInviteCodeResponse
import felix.projekt.livinglink.server.groups.domain.GetGroupsResponse
import felix.projekt.livinglink.server.groups.domain.Group
import felix.projekt.livinglink.server.groups.domain.JoinGroupResponse
import felix.projekt.livinglink.server.groups.interfaces.CreateGroupUseCase
import felix.projekt.livinglink.server.groups.interfaces.CreateInviteCodeUseCase
import felix.projekt.livinglink.server.groups.interfaces.DeleteInviteCodeUseCase
import felix.projekt.livinglink.server.groups.interfaces.GetUserGroupsUseCase
import felix.projekt.livinglink.server.groups.interfaces.JoinGroupWithInviteCodeUseCase
import felix.projekt.livinglink.shared.groups.requestModel.GroupRequest
import felix.projekt.livinglink.shared.groups.requestModel.GroupResponse
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.groupRoutes(
    groupsConfig: GroupsConfig,
    getUserGroupsUseCase: GetUserGroupsUseCase,
    createGroupUseCase: CreateGroupUseCase,
    createInviteCodeUseCase: CreateInviteCodeUseCase,
    deleteInviteCodeUseCase: DeleteInviteCodeUseCase,
    joinGroupWithInviteCodeUseCase: JoinGroupWithInviteCodeUseCase
) {
    fun Group.toResponse() = GroupResponse.Group(
        id = this.id,
        name = this.name,
        memberIdToMember = this.memberIdToMember.mapValues { member ->
            GroupResponse.Group.Member(
                id = member.value.id,
                username = member.value.username
            )
        },
        inviteCodes = this.inviteCodeIdToInviteCode.values.map { inviteCode ->
            GroupResponse.Group.InviteCode(
                id = inviteCode.id,
                name = inviteCode.name,
                creatorId = inviteCode.creatorId,
                usages = inviteCode.usages
            )
        },
        version = this.version
    )

    route("/groups") {
        post("") {
            val request: GroupRequest.GetGroups = call.receive()
            val response: GetGroupsResponse = getUserGroupsUseCase(
                userId = call.userId,
                currentGroupVersions = request.currentGroupVersions
            )
            when (response) {
                is GetGroupsResponse.NotModified -> {
                    call.respond<GroupResponse.GetGroups>(
                        GroupResponse.GetGroups.NotModified(
                            nextPollAfterMillis = groupsConfig.notModifiedPollAfterMillis
                        )
                    )
                }

                is GetGroupsResponse.Success -> {
                    val groups = response.groups.mapValues { group ->
                        group.value.toResponse()
                    }
                    call.respond<GroupResponse.GetGroups>(
                        GroupResponse.GetGroups.Success(
                            groups = groups,
                            nextPollAfterMillis = groupsConfig.defaultPollAfterMillis
                        )
                    )
                }
            }
        }

        post("/create") {
            val request: GroupRequest.CreateGroup = call.receive()
            val group = createGroupUseCase(
                userId = call.userId,
                username = call.username,
                groupName = request.groupName
            )
            call.respond<GroupResponse.CreateGroup>(
                GroupResponse.CreateGroup.Success(group.toResponse())
            )
        }

        post("/inviteCode/create") {
            val request: GroupRequest.CreateInviteCode = call.receive()
            val response = createInviteCodeUseCase(
                userId = call.userId,
                groupId = request.groupId,
                inviteCodeName = request.inviteCodeName
            )
            when (response) {
                is CreateInviteCodeResponse.Success -> {
                    call.respond<GroupResponse.CreateInviteCode>(
                        GroupResponse.CreateInviteCode.Success(key = response.key)
                    )
                }
            }
        }

        post("/inviteCode/delete") {
            val request: GroupRequest.DeleteInviteCode = call.receive()
            val response = deleteInviteCodeUseCase(
                userId = call.userId,
                groupId = request.groupId,
                inviteCodeId = request.inviteCodeId
            )
            when (response) {
                is DeleteInviteCodeResponse.Success -> {
                    call.respond<GroupResponse.DeleteInviteCode>(
                        GroupResponse.DeleteInviteCode.Success
                    )
                }
            }
        }

        post("/inviteCode/join") {
            val request: GroupRequest.JoinGroup = call.receive()
            val response = joinGroupWithInviteCodeUseCase(
                userId = call.userId,
                username = call.username,
                inviteCodeKey = request.inviteCodeKey
            )

            when (response) {
                is JoinGroupResponse.Success -> {
                    call.respond<GroupResponse.JoinGroup>(
                        GroupResponse.JoinGroup.Success(group = response.group.toResponse())
                    )
                }

                is JoinGroupResponse.InviteCodeNotFound -> {
                    call.respond<GroupResponse.JoinGroup>(GroupResponse.JoinGroup.InviteCodeNotFound)
                }

                is JoinGroupResponse.AlreadyMember -> {
                    call.respond<GroupResponse.JoinGroup>(GroupResponse.JoinGroup.AlreadyMember)
                }
            }
        }
    }
}