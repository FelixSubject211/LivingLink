package felix.projekt.livinglink.shared.groups.requestModel

import kotlinx.serialization.Serializable

@Serializable
sealed class GroupResponse {
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
    data class Group(
        val id: String,
        val name: String,
        val memberIdToMember: Map<String, Member>,
        val version: Long
    ) {
        @Serializable
        data class Member(
            val id: String,
            val username: String
        )
    }
}