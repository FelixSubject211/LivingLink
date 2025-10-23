package felix.projekt.livinglink.server.auth.interfaces

import felix.projekt.livinglink.server.auth.domain.RegisterResponse

interface RegisterUserUseCase {
    suspend operator fun invoke(username: String, password: String): RegisterResponse
}