package felix.projekt.livinglink.server.auth.domain

sealed class RefreshResponse {
    data class Success(val tokenResponse: TokenResponse) : RefreshResponse()

    data object TokenExpired : RefreshResponse()
}