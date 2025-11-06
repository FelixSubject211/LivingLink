package felix.projekt.livinglink.composeApp.groups.domain

data class Group(
    val id: String,
    val name: String,
    val memberIdToMember: Map<String, Member>,
    val inviteCodes: List<InviteCode>,
    val version: Long
) {
    data class Member(
        val id: String,
        val username: String
    )

    data class InviteCode(
        val id: String,
        val name: String,
        val creatorId: String,
        val usages: Int
    )
}