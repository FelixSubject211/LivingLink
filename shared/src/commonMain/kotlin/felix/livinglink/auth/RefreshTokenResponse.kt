package felix.livinglink.auth

import kotlinx.serialization.Serializable

@Serializable
sealed class RefreshTokenResponse {
    @Serializable
    data class Success(val accessToken: String, val refreshToken: String) : RefreshTokenResponse()

    @Serializable
    data object InvalidOrExpiredRefreshToken : RefreshTokenResponse()
}