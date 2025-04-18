package felix.livinglink.auth

import felix.livinglink.common.BaseIntegrationTest
import felix.livinglink.common.UsersTable
import felix.livinglink.common.assertHasTotalRecords
import felix.livinglink.common.defaultAppModule
import felix.livinglink.common.delete
import felix.livinglink.common.registerUser
import felix.livinglink.module
import io.ktor.client.request.delete
import io.ktor.server.testing.testApplication
import org.ktorm.dsl.delete
import org.ktorm.dsl.eq
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthRoutesDeleteUserTest : BaseIntegrationTest() {

    @Test
    fun `should delete user successfully if authenticated`() = testApplication {

        val username = "username"
        val password = "hashedPassword"

        // Arrange
        application {
            module(
                config = config,
                appModule = defaultAppModule(config = config)
            )
        }

        val registerResponse = client.registerUser(username = username, password = password)

        // Act
        val deleteResponse: DeleteUserResponse = client.delete(
            urlString = "auth/account",
            token = registerResponse.accessToken
        )

        // Assert
        assertEquals(DeleteUserResponse.Success, deleteResponse)
        database.assertHasTotalRecords(UsersTable, totalRecords = 0)
    }

    @Test
    fun `should fail with 401 if not authenticated`() = testApplication {

        // Arrange
        application {
            module(
                config = config,
                appModule = defaultAppModule(config = config)
            )
        }

        // Act
        val response = client.delete("auth/account") {}

        // Assert
        assertEquals(401, response.status.value)
    }

    @Test
    fun `should return Error if user does not exist in DB`() = testApplication {
        val username = "ghostUser"
        val password = "testPassword123"

        // Arrange
        application {
            module(
                config = config,
                appModule = defaultAppModule(config = config)
            )
        }

        val registerResponse = client.registerUser(username = username, password = password)

        database.delete(UsersTable) {
            it.username eq username
        }

        // Act
        val deleteResponse: DeleteUserResponse = client.delete(
            urlString = "auth/account",
            token = registerResponse.accessToken
        )

        // Assert
        assertEquals(DeleteUserResponse.Error, deleteResponse)
    }
}