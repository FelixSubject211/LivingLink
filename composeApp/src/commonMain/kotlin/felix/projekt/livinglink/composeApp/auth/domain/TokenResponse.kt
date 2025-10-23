package felix.projekt.livinglink.composeApp.auth.domain

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)