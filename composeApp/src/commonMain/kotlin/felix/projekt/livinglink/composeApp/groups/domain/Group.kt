package felix.projekt.livinglink.composeApp.groups.domain

data class Group(
    val id: String,
    val name: String,
    val memberIdToMember: Map<String, Member>,
    val version: Long
) {
    data class Member(
        val id: String,
        val username: String
    )
}