package felix.livinglink

interface Config {
    val serverPort: Int
    val authenticationConfig: String
    val userIdClaim: String
    val usernameClaim: String
    val sessionIdClaim: String
    val accessTokenExpirationMs: Int
    val refreshTokenExpirationMs: Int
}

fun defaultConfig(): Config {
    return object : Config {
        override val serverPort = 8080
        override val authenticationConfig = "auth-jwt"
        override val userIdClaim = "userId"
        override val usernameClaim = "username"
        override val sessionIdClaim = "sessionId"
        override val accessTokenExpirationMs = 1000 * 60 * 60
        override val refreshTokenExpirationMs = 1000 * 60 * 60 * 24 * 7
    }
}