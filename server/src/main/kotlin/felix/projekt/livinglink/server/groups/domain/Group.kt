package felix.projekt.livinglink.server.groups.domain

import kotlinx.serialization.Serializable

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

    fun addMember(userId: String, username: String) = copy(
        memberIdToMember = memberIdToMember + (userId to Member(userId, username))
    )

    fun removeMember(userId: String) = copy(
        memberIdToMember = memberIdToMember - userId
    )

    fun isSingleMember(userId: String) = memberIdToMember.size == 1 && memberIdToMember.containsKey(userId)
}