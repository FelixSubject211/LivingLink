package felix.projekt.livinglink.server.groups.interfaces

fun interface CheckGroupMembershipService {
    suspend operator fun invoke(userId: String, groupId: String): Boolean
}