package felix.livinglink.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import felix.livinglink.common.RefreshTokensTable
import felix.livinglink.common.ServerConfig
import felix.livinglink.common.UsersTable
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.where
import org.mindrot.jbcrypt.BCrypt
import kotlin.test.assertEquals
import kotlin.test.assertTrue

fun Database.assertHasUser(
    userId: String,
    username: String,
    password: String,
): User {
    val users = this
        .from(UsersTable)
        .select(UsersTable.id, UsersTable.username, UsersTable.hashedPassword)
        .where { UsersTable.username eq username }
        .map { row ->
            User(
                id = row[UsersTable.id]!!,
                username = row[UsersTable.username]!!,
                hashedPassword = row[UsersTable.hashedPassword]!!
            )
        }

    assertTrue(users.isNotEmpty(), "User with username '$username' was not found")
    val user = users.first()

    assertEquals(userId, user.id)
    assertEquals(username, user.username)
    assertTrue(BCrypt.checkpw(password, user.hashedPassword))

    return user
}

fun Database.assertHasRefreshToken(
    config: ServerConfig,
    refreshTokenToken: String,
    userId: String,
    currentTimeMills: Long,
): RefreshToken {
    val refreshTokens = this
        .from(RefreshTokensTable)
        .select(
            RefreshTokensTable.token,
            RefreshTokensTable.userId,
            RefreshTokensTable.username,
            RefreshTokensTable.expiresAt
        )
        .where { RefreshTokensTable.token eq refreshTokenToken }
        .map { row ->
            RefreshToken(
                token = row[RefreshTokensTable.token]!!,
                userId = row[RefreshTokensTable.userId]!!,
                username = row[RefreshTokensTable.username]!!,
                expiresAt = row[RefreshTokensTable.expiresAt]!!
            )
        }

    assertTrue(refreshTokens.isNotEmpty(), "Refresh token '$refreshTokenToken' was not found")
    val refreshToken = refreshTokens.first()

    assertEquals(refreshTokenToken, refreshToken.token)
    assertEquals(userId, refreshToken.userId)
    assertEquals(
        expected = currentTimeMills + config.refreshTokenExpirationMs,
        actual = refreshToken.expiresAt
    )

    return refreshToken
}


fun assertAccessTokenIsValid(
    config: ServerConfig,
    accessToken: String,
    userId: String,
    username: String,
    sessionId: String,
    currentTimeMills: Long,
) {
    val verifier: JWTVerifier = JWT.require(Algorithm.HMAC256(config.secret))
        .withIssuer(config.issuer)
        .withAudience(config.jwtAudience)
        .build()

    val decodedToken = verifier.verify(accessToken)

    assertEquals(userId, decodedToken.getClaim(config.userIdClaim).asString())
    assertEquals(username, decodedToken.getClaim(config.usernameClaim).asString())
    assertEquals(sessionId, decodedToken.getClaim(config.sessionIdClaim).asString())

    val accessTokenExpiresAt = decodedToken.expiresAt.time
    val expectedExpiresAt = currentTimeMills + config.accessTokenExpirationMs
    assertTrue(
        actual = accessTokenExpiresAt in (expectedExpiresAt - 3000)..(expectedExpiresAt + 3000),
        message = "Expected expiration time within ±3000ms, but was $accessTokenExpiresAt"
    )
}