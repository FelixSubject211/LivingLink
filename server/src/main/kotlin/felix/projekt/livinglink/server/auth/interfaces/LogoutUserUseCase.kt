package felix.projekt.livinglink.server.auth.interfaces

interface LogoutUserUseCase {
    suspend operator fun invoke(refreshToken: String)
}