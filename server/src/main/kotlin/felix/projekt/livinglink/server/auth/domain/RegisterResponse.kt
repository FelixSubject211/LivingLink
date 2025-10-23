package felix.projekt.livinglink.server.auth.domain

sealed class RegisterResponse {
    data class Success(val tokenResponse: TokenResponse) : RegisterResponse()

    data object UserAlreadyExists : RegisterResponse()

    data object PolicyViolation : RegisterResponse()
}