package felix.livinglink.auth

import felix.livinglink.common.BaseIntegrationTest
import felix.livinglink.common.RefreshTokensTable
import felix.livinglink.common.TimeService
import felix.livinglink.common.UsersTable
import felix.livinglink.common.UuidFactory
import felix.livinglink.common.assertHasTotalRecords
import felix.livinglink.common.defaultAppModule
import felix.livinglink.common.post
import felix.livinglink.common.registerUser
import felix.livinglink.module
import io.ktor.server.testing.testApplication
import org.junit.Test
import kotlin.test.assertEquals

class AuthRoutesLoginTest : BaseIntegrationTest() {

    @Test
    fun `should return tokens and persist refresh token on successful login`() = testApplication {
        val username = "username"
        val password = "password"
        val userId = "userId"
        val registerAccessTokenSessionId = "registerAccessTokenSessionId"
        val registerRefreshTokenToken = "registerRefreshTokenToken"
        val loginAccessTokenSessionId = " loginAccessTokenSessionId"
        val loginRefreshTokenToken = "loginRefreshTokenToken"
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
                    3 -> loginAccessTokenSessionId
                    4 -> loginRefreshTokenToken
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

        client.registerUser(username = username, password = password)

        // Act
        val loginResponse: LoginResponse = client.post(
            urlString = "auth/login",
            request = LoginRequest(username = username, password = password)
        )

        // Assert
        when (loginResponse) {
            is LoginResponse.Success -> {
                assertAccessTokenIsValid(
                    config = config,
                    accessToken = loginResponse.accessToken,
                    userId = userId,
                    username = username,
                    sessionId = loginAccessTokenSessionId,
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
                    refreshTokenToken = loginRefreshTokenToken,
                    userId = userId,
                    currentTimeMills = currentTimeMills,
                )
                database.assertHasTotalRecords(RefreshTokensTable, totalRecords = 2)
                assertEquals(loginResponse.refreshToken, refreshToken.token)
            }

            else -> error("Unexpected response type $loginResponse")
        }
    }

    @Test
    fun `should return InvalidUsernameOrPassword if user not exits`() = testApplication {
        val username = "username"
        val password = "password"

        // Arrange
        application {
            module(
                config = config,
                appModule = defaultAppModule(config = config)
            )
        }

        // Act
        val response: LoginResponse = client.post(
            urlString = "auth/login",
            request = LoginRequest(username = username, password = password)
        )

        // Assert
        assertEquals(LoginResponse.InvalidUsernameOrPassword, response)
        database.assertHasTotalRecords(UsersTable, RefreshTokensTable, totalRecords = 0)
    }

    @Test
    fun `should return InvalidUsernameOrPassword if password ist false`() = testApplication {
        val username = "username"
        val password = "password"
        val falsePassword = "falsePassword"

        // Arrange
        application {
            module(
                config = config,
                appModule = defaultAppModule(config = config)
            )
        }

        client.registerUser(username = username, password = password)

        // Act
        val loginResponse: LoginResponse = client.post(
            urlString = "auth/login",
            request = LoginRequest(username = username, password = falsePassword)
        )

        // Assert
        assertEquals(LoginResponse.InvalidUsernameOrPassword, loginResponse)
        database.assertHasTotalRecords(UsersTable, RefreshTokensTable, totalRecords = 1)
    }
}