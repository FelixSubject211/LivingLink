package felix.projekt.livinglink.composeApp.auth.interfaces

interface RegisterUserUseCase {
    suspend operator fun invoke(username: String, password: String): Response

    sealed class Response {
        data object Success : Response()
        data object UserAlreadyExists : Response()
        data object PolicyViolation : Response()
        data object NetworkError : Response()
    }
}