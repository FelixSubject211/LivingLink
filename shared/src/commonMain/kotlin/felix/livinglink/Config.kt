package felix.livinglink

interface Config {
    val serverHost: String
    val serverPort: Int
    val authenticationConfig: String
    val userIdClaim: String
    val usernameClaim: String
    val sessionIdClaim: String
    val groupIdsClaim: String
    val accessTokenExpirationMs: Int
    val refreshTokenExpirationMs: Int
    val pollingIntervalSeconds: Int
    val pollingRetryDelaySeconds: Int
    val aggregateTimeoutSeconds: Int
    val eventSourcingAppendRetryCount: Int
    val eventSourcingAppendRetryDelayMs: Long
}

fun defaultConfig(): Config {
    return object : Config {
        override val serverHost = getLocalhost()
        override val serverPort = 8080
        override val authenticationConfig = "auth-jwt"
        override val userIdClaim = "userId"
        override val usernameClaim = "username"
        override val sessionIdClaim = "sessionId"
        override val groupIdsClaim = "groupIds"
        override val accessTokenExpirationMs = 1000 * 60 * 60
        override val refreshTokenExpirationMs = 1000 * 60 * 60 * 24 * 7
        override val pollingIntervalSeconds = 5
        override val pollingRetryDelaySeconds = 10
        override val aggregateTimeoutSeconds = 5
        override val eventSourcingAppendRetryCount = 10
        override val eventSourcingAppendRetryDelayMs = 500L
    }
}