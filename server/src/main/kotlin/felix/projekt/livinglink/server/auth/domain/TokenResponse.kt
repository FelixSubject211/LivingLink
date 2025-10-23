package felix.projekt.livinglink.server.auth.domain

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)