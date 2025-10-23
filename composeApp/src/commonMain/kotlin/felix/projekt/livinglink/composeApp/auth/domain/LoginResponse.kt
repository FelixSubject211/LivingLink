package felix.projekt.livinglink.composeApp.auth.domain

sealed class LoginResponse {
    data class Success(val tokenResponse: TokenResponse) : LoginResponse()
    data object InvalidCredentials : LoginResponse()
}