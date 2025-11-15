package felix.projekt.livinglink.shared.groups.requestModel

import kotlinx.serialization.Serializable

sealed interface GroupRequest {
    @Serializable
    data class GetGroups(val currentGroupVersions: Map<String, Long>)

    @Serializable
    data class CreateGroup(val groupName: String)

    @Serializable
    data class CreateInviteCode(
        val groupId: String,
        val inviteCodeName: String
    )

    @Serializable
    data class DeleteInviteCode(
        val groupId: String,
        val inviteCodeId: String
    )

    @Serializable
    data class JoinGroup(
        val inviteCodeKey: String
    )
}