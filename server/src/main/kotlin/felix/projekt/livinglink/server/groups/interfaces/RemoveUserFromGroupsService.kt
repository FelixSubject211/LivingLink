package felix.projekt.livinglink.server.groups.interfaces

interface RemoveUserFromGroupsService {
    suspend operator fun invoke(userId: String)
}