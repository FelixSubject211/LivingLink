package felix.projekt.livinglink.server.auth.application

import felix.projekt.livinglink.server.auth.domain.AuthClient
import felix.projekt.livinglink.server.auth.domain.LoginResponse
import felix.projekt.livinglink.server.auth.interfaces.LoginUserUseCase

class LoginUserDefaultUseCase(
    private val authClient: AuthClient
) : LoginUserUseCase {
    override suspend fun invoke(username: String, password: String): LoginResponse {
        return authClient.login(username = username, password = password)
    }
}