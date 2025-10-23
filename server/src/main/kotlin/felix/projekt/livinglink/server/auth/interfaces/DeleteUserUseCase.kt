package felix.projekt.livinglink.server.auth.interfaces

interface DeleteUserUseCase {
    suspend operator fun invoke(userId: String, username: String)
}