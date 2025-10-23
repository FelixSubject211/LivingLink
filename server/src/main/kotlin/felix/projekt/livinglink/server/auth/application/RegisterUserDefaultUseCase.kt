package felix.projekt.livinglink.server.auth.application

import felix.projekt.livinglink.server.auth.domain.AuthClient
import felix.projekt.livinglink.server.auth.domain.RegisterResponse
import felix.projekt.livinglink.server.auth.interfaces.RegisterUserUseCase

class RegisterUserDefaultUseCase(
    private val authClient: AuthClient
) : RegisterUserUseCase {
    override suspend fun invoke(username: String, password: String): RegisterResponse {
        return authClient.register(username = username, password = password)
    }
}