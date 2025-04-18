package felix.livinglink.auth

data class RefreshToken(
    val token: String,
    val userId: String,
    val username: String,
    val expiresAt: Long
)