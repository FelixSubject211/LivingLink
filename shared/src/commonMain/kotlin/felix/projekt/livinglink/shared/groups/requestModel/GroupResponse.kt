package felix.projekt.livinglink.shared.groups.requestModel

import kotlinx.serialization.Serializable

sealed interface GroupResponse {
    @Serializable
    sealed class GetGroups() {
        @Serializable
        data class Success(
            val groups: Map<String, Group>,
            val nextPollAfterMillis: Long
        ) : GetGroups()

        @Serializable
        data class NotModified(
            val nextPollAfterMillis: Long
        ) : GetGroups()
    }

    @Serializable
    sealed class CreateGroup {
        @Serializable
        data class Success(val group: Group) : CreateGroup()
    }

    @Serializable
    sealed class CreateInviteCode {
        @Serializable
        data class Success(val key: String) : CreateInviteCode()
    }

    @Serializable
    sealed class DeleteInviteCode {
        @Serializable
        data object Success : DeleteInviteCode()
    }

    @Serializable
    sealed class JoinGroup {
        @Serializable
        data class Success(val group: Group) : JoinGroup()

        @Serializable
        data object InviteCodeNotFound : JoinGroup()

        @Serializable
        data object AlreadyMember : JoinGroup()
    }

    @Serializable
    data class Group(
        val id: String,
        val name: String,
        val memberIdToMember: Map<String, Member>,
        val inviteCodes: List<InviteCode>,
        val version: Long
    ) {
        @Serializable
        data class Member(
            val id: String,
            val username: String
        )

        @Serializable
        data class InviteCode(
            val id: String,
            val name: String,
            val creatorId: String,
            val usages: Int
        )
    }
}