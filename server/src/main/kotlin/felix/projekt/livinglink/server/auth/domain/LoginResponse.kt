package felix.projekt.livinglink.server.auth.domain

sealed class LoginResponse {
    data class Success(val tokenResponse: TokenResponse) : LoginResponse()

    data object InvalidCredentials : LoginResponse()
}