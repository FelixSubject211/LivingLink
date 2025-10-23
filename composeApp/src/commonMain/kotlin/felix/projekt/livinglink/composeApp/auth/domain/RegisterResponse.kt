package felix.projekt.livinglink.composeApp.auth.domain

sealed class RegisterResponse {
    data class Success(val tokenResponse: TokenResponse) : RegisterResponse()

    data object UserAlreadyExists : RegisterResponse()

    data object PolicyViolation : RegisterResponse()
}