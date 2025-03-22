package felix.livinglink.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import felix.livinglink.common.ServerConfig
import felix.livinglink.common.TimeService
import felix.livinglink.common.UuidFactory
import java.util.Date

interface JwtService {
    val verifier: JWTVerifier
    fun generateToken(userId: String, username: String): String
    fun generateRefreshToken(userId: String, username: String): RefreshToken
}

class JwtDefaultService(
    private val config: ServerConfig,
    private val timeService: TimeService,
    private val uuidFactory: UuidFactory
) : JwtService {
    private val algorithm = Algorithm.HMAC256(config.secret)

    override val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(config.issuer)
        .withAudience(config.jwtAudience)
        .build()

    override fun generateToken(userId: String, username: String): String {
        return JWT.create()
            .withIssuer(config.issuer)
            .withAudience(config.jwtAudience)
            .withClaim(config.userIdClaim, userId)
            .withClaim(config.usernameClaim, username)
            .withClaim(config.sessionIdClaim, uuidFactory())
            .withExpiresAt(Date(timeService.currentTimeMillis() + config.accessTokenExpirationMs))
            .sign(algorithm)
    }

    override fun generateRefreshToken(userId: String, username: String): RefreshToken {
        val token = uuidFactory()
        val expiresAt = timeService.currentTimeMillis() + config.refreshTokenExpirationMs
        return RefreshToken(
            token = token,
            userId = userId,
            username = username,
            expiresAt = expiresAt
        )
    }
}