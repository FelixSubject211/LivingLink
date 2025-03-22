package felix.livinglink.auth

import felix.livinglink.common.BaseIntegrationTest
import felix.livinglink.common.RawUser
import felix.livinglink.common.RefreshTokensTable
import felix.livinglink.common.TimeService
import felix.livinglink.common.UsersTable
import felix.livinglink.common.UuidFactory
import felix.livinglink.common.addSampleData
import felix.livinglink.common.assertHasTotalRecords
import felix.livinglink.common.defaultAppModule
import felix.livinglink.common.post
import felix.livinglink.module
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthRoutesRegisterTest : BaseIntegrationTest() {
    @Test
    fun `should return tokens and persist user and refresh token on successful registration`() =
        testApplication {
            val username = "username"
            val password = "password"
            val userId = "userId"
            val accessTokenSessionId = "accessTokenSessionId"
            val refreshTokenToken = "refreshToken"
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
                        1 -> accessTokenSessionId
                        2 -> refreshTokenToken
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

            // Act
            val response: RegisterResponse = client.post(
                urlString = "auth/register",
                request = RegisterRequest(username = username, password = password)
            )

            // Assert
            when (response) {
                is RegisterResponse.Success -> {
                    assertAccessTokenIsValid(
                        config = config,
                        accessToken = response.accessToken,
                        userId = userId,
                        username = username,
                        sessionId = accessTokenSessionId,
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
                        refreshTokenToken = refreshTokenToken,
                        userId = userId,
                        currentTimeMills = currentTimeMills,
                    )
                    database.assertHasTotalRecords(RefreshTokensTable, totalRecords = 1)
                    assertEquals(response.refreshToken, refreshToken.token)
                }

                else -> error("Unexpected response type $response")
            }
        }

    @Test
    fun `should return UsernameTooShort if username is too short`() = testApplication {
        val username = "1234567"
        val shortPassword = "validUsername"

        // Arrange
        application {
            module(
                config = config,
                appModule = defaultAppModule(config = config)
            )
        }

        // Act
        val response: RegisterResponse = client.post(
            urlString = "auth/register",
            request = RegisterRequest(username = username, password = shortPassword)
        )

        // Assert
        assertEquals(RegisterResponse.UsernameTooShort(minLength = 8), response)
        database.assertHasTotalRecords(UsersTable, RefreshTokensTable, totalRecords = 0)
    }

    @Test
    fun `should return PasswordTooShort if password is too short`() = testApplication {
        val username = "validUsername"
        val shortPassword = "1234567"

        // Arrange
        application {
            module(
                config = config,
                appModule = defaultAppModule(config = config)
            )
        }

        // Act
        val response: RegisterResponse = client.post(
            urlString = "auth/register",
            request = RegisterRequest(username = username, password = shortPassword)
        )

        // Assert
        assertEquals(RegisterResponse.PasswordTooShort(minLength = 8), response)
        database.assertHasTotalRecords(UsersTable, RefreshTokensTable, totalRecords = 0)
    }

    @Test
    fun `should return UserAlreadyExists if username is already taken`() = testApplication {
        val user = RawUser(
            id = "id",
            username = "existingUser",
            password = "password"
        )

        // Arrange
        application {
            module(
                config = config,
                appModule = defaultAppModule(config = config)
            )
        }

        database.addSampleData(user = user)

        // Act
        val response: RegisterResponse = client.post(
            urlString = "auth/register",
            request = RegisterRequest(username = user.username, password = user.password)
        )

        // Assert
        assertEquals(RegisterResponse.UserAlreadyExists, response)

        database.assertHasTotalRecords(UsersTable, totalRecords = 1)
        database.assertHasTotalRecords(RefreshTokensTable, totalRecords = 0)
    }
}