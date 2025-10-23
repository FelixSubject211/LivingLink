package felix.projekt.livinglink.server.auth.interfaces

import felix.projekt.livinglink.server.auth.domain.LoginResponse

interface LoginUserUseCase {
    suspend operator fun invoke(username: String, password: String): LoginResponse
}