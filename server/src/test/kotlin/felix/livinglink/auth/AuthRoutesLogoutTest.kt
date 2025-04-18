package felix.livinglink.auth

import felix.livinglink.common.BaseIntegrationTest
import felix.livinglink.common.RefreshTokensTable
import felix.livinglink.common.UsersTable
import felix.livinglink.common.assertHasTotalRecords
import felix.livinglink.common.defaultAppModule
import felix.livinglink.common.post
import felix.livinglink.common.registerUser
import felix.livinglink.module
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthRoutesLogoutTest : BaseIntegrationTest() {

    @Test
    fun `should logout successfully and remove refresh token from store`() = testApplication {
        val username = "testUser"
        val password = "testPassword123"

        // Arrange
        application {
            module(
                config = config,
                appModule = defaultAppModule(config = config)
            )
        }

        val registerResponse = client.registerUser(username = username, password = password)

        // Act
        val logoutResponse: LogoutResponse = client.post(
            urlString = "auth/logout",
            request = LogoutRequest(refreshToken = registerResponse.refreshToken)
        )

        // Assert
        assertEquals(LogoutResponse.Success, logoutResponse)
        database.assertHasTotalRecords(UsersTable, totalRecords = 1)
        database.assertHasTotalRecords(RefreshTokensTable, totalRecords = 0)
    }

    @Test
    fun `should return InvalidRefreshToken if token is not found`() = testApplication {
        val refreshToken = "doesNotExist"

        // Arrange
        application {
            module(
                config = config,
                appModule = defaultAppModule(config = config)
            )
        }

        // Act
        val logoutResponse: LogoutResponse = client.post(
            urlString = "auth/logout",
            request = LogoutRequest(refreshToken = refreshToken)
        )

        // Assert
        assertEquals(LogoutResponse.InvalidRefreshToken, logoutResponse)
        database.assertHasTotalRecords(UsersTable, RefreshTokensTable, totalRecords = 0)
    }
}