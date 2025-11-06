package felix.projekt.livinglink.server.groups.domain

data class Group(
    val id: String,
    val name: String,
    val memberIdToMember: Map<String, Member>,
    val inviteCodeIdToInviteCode: Map<String, InviteCode>,
    val version: Long
) {
    data class Member(
        val id: String,
        val username: String
    )

    data class InviteCode(
        val id: String,
        val key: String,
        val name: String,
        val creatorId: String,
        val usages: Int
    )

    fun addMember(userId: String, username: String) = copy(
        memberIdToMember = memberIdToMember + (userId to Member(userId, username))
    )

    fun removeMember(userId: String) = copy(
        memberIdToMember = memberIdToMember - userId
    )

    fun isSingleMember(userId: String) = memberIdToMember.size == 1 && memberIdToMember.containsKey(userId)

    fun addInviteCode(inviteCode: InviteCode) = copy(
        inviteCodeIdToInviteCode = inviteCodeIdToInviteCode + (inviteCode.id to inviteCode)
    )

    fun removeInviteCode(inviteCodeId: String) = copy(
        inviteCodeIdToInviteCode = inviteCodeIdToInviteCode - inviteCodeId
    )
}