package felix.projekt.livinglink.server.auth.application

import felix.projekt.livinglink.server.auth.domain.AuthClient
import felix.projekt.livinglink.server.auth.interfaces.DeleteUserUseCase
import felix.projekt.livinglink.server.groups.interfaces.RemoveUserFromGroupsService

class DeleteUserDefaultUseCase(
    private val removeUserFromGroupsService: RemoveUserFromGroupsService,
    private val authClient: AuthClient
) : DeleteUserUseCase {
    override suspend fun invoke(userId: String, username: String) {
        removeUserFromGroupsService(userId = userId)
        return authClient.deleteUser(username)
    }
}