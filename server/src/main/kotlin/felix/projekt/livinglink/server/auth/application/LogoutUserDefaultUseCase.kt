package felix.projekt.livinglink.server.auth.application

import felix.projekt.livinglink.server.auth.domain.AuthClient
import felix.projekt.livinglink.server.auth.interfaces.LogoutUserUseCase

class LogoutUserDefaultUseCase(
    private val authClient: AuthClient
) : LogoutUserUseCase {
    override suspend fun invoke(refreshToken: String) {
        return authClient.logout(refreshToken)
    }
}
