package felix.projekt.livinglink.composeApp.auth.interfaces

interface LoginUserUseCase {
    suspend operator fun invoke(username: String, password: String): Response

    sealed class Response {
        data object Success : Response()
        data object InvalidCredentials : Response()
        data object NetworkError : Response()
    }
}