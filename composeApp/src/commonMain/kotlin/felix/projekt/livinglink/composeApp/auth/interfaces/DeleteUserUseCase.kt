package felix.projekt.livinglink.composeApp.auth.interfaces

interface DeleteUserUseCase {
    suspend operator fun invoke(): Response

    sealed class Response {
        data object Success : Response()
        data object Unauthorized : Response()
        data object NetworkError : Response()
    }
}