package felix.livinglink.auth

import felix.livinglink.common.BaseIntegrationTest
import felix.livinglink.common.RawUser
import felix.livinglink.common.RefreshTokensTable
import felix.livinglink.common.TimeService
import felix.livinglink.common.UsersTable
import felix.livinglink.common.UuidFactory
import felix.livinglink.common.addSampleUsers
import felix.livinglink.common.assertHasTotalRecords
import felix.livinglink.common.defaultAppModule
import felix.livinglink.common.post
import felix.livinglink.module
import io.ktor.server.testing.testApplication
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthRoutesRefreshTest : BaseIntegrationTest() {

    @Test
    fun `should return new access token and refresh token on valid refresh token`() =
        testApplication {
            val username = "username"
            val password = "password"
            val userId = "userId"
            val registerAccessTokenSessionId = "registerAccessTokenSessionId"
            val registerRefreshTokenToken = "registerRefreshTokenToken"
            val refreshAccessTokenSessionId = "refreshAccessTokenSessionId"
            val refreshRefreshTokenToken = "refreshRefreshTokenToken"
            val currentTimeMills = System.currentTimeMillis()

            // Arrange
            val timeService: TimeService = object : TimeService {
                override fun currentTimeMillis(): Long {
                    return currentTimeMills
                }
            }

            val uuidFactory: UuidFactory = object : UuidFactory {
                var callCount = 0

                override fun invoke(): String {
                    return when (callCount) {
                        0 -> userId
                        1 -> registerAccessTokenSessionId
                        2 -> registerRefreshTokenToken
                        3 -> refreshAccessTokenSessionId
                        4 -> refreshRefreshTokenToken
                        else -> {
                            error("Unexpected call")
                        }
                    }.also { callCount++ }
                }
            }

            application {
                module(
                    config = config,
                    appModule = defaultAppModule(
                        config = config,
                        timeService = timeService,
                        uuidFactory = uuidFactory
                    )
                )
            }

            val registerResponse: RegisterResponse = client.post(
                urlString = "auth/register",
                request = RegisterRequest(username = username, password = password)
            )

            assertTrue(registerResponse is RegisterResponse.Success)

            // Act
            val refreshResponse: RefreshTokenResponse = client.post(
                urlString = "auth/refresh",
                request = RefreshTokenRequest(refreshToken = registerResponse.refreshToken)
            )

            // Assert
            when (refreshResponse) {
                is RefreshTokenResponse.Success -> {
                    assertAccessTokenIsValid(
                        config = config,
                        accessToken = refreshResponse.accessToken,
                        userId = userId,
                        username = username,
                        sessionId = refreshAccessTokenSessionId,
                        currentTimeMills = currentTimeMills
                    )

                    database.assertHasUser(
                        userId = userId,
                        username = username,
                        password = password,
                    )
                    database.assertHasTotalRecords(UsersTable, totalRecords = 1)

                    val refreshToken = database.assertHasRefreshToken(
                        config = config,
                        refreshTokenToken = refreshRefreshTokenToken,
                        userId = userId,
                        currentTimeMills = currentTimeMills,
                    )
                    database.assertHasTotalRecords(RefreshTokensTable, totalRecords = 1)
                    assertEquals(refreshResponse.refreshToken, refreshToken.token)
                }

                else -> error("Unexpected response type $refreshResponse")
            }
        }

    @Test
    fun `should return InvalidOrExpiredRefreshToken if refresh token is not in store`() =
        testApplication {

            // Arrange
            application {
                module(
                    config = config,
                    appModule = defaultAppModule(config = config)
                )
            }

            // Act
            val refreshResponse: RefreshTokenResponse = client.post(
                urlString = "auth/refresh",
                request = RefreshTokenRequest(refreshToken = "notExistingRefreshToken")
            )

            // Assert
            when (refreshResponse) {
                is RefreshTokenResponse.InvalidOrExpiredRefreshToken -> {
                    database.assertHasTotalRecords(UsersTable, RefreshTokensTable, totalRecords = 0)
                }

                else -> error("Unexpected response type $refreshResponse")
            }
        }

    @Test
    fun `should return InvalidOrExpiredRefreshToken if refresh token is expired`() =
        testApplication {

            val currentTimeMillis: Long = 1_000_000_000
            val user = RawUser(
                id = "userId",
                username = "username",
                password = "password"
            )
            val refreshToken = RefreshToken(
                token = "token",
                userId = user.id,
                username = user.username,
                expiresAt = 999_999_999
            )

            // Arrange
            val timeService: TimeService = object : TimeService {
                override fun currentTimeMillis(): Long {
                    return currentTimeMillis
                }
            }

            application {
                module(
                    config = config,
                    appModule = defaultAppModule(
                        config = config,
                        timeService = timeService
                    )
                )
            }

            database.addSampleUsers(
                user = user,
                refreshToken = refreshToken
            )

            // Act
            val refreshResponse: RefreshTokenResponse = client.post(
                urlString = "auth/refresh",
                request = RefreshTokenRequest(refreshToken = "notExistingRefreshToken")
            )

            // Assert
            when (refreshResponse) {
                is RefreshTokenResponse.InvalidOrExpiredRefreshToken -> {
                    database.assertHasTotalRecords(UsersTable, RefreshTokensTable, totalRecords = 1)
                }

                else -> error("Unexpected response type $refreshResponse")
            }
        }
}