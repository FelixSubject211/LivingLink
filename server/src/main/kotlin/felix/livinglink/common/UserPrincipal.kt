package felix.livinglink.common

data class UserPrincipal(
    val userId: String,
    val username: String,
    val sessionId: String,
    val groupIds: List<String>
) {
    fun hasAccessToGroup(groupId: String): Boolean = groupId in groupIds
}