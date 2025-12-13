package felix.projekt.livinglink.server.groups.interfaces

interface CheckGroupMembershipService {
    suspend operator fun invoke(userId: String, groupId: String): Boolean
}