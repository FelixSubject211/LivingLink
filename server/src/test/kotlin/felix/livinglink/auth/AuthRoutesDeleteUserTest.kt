package felix.livinglink.auth

import felix.livinglink.common.BaseIntegrationTest
import felix.livinglink.common.TestData
import felix.livinglink.common.UsersTable
import felix.livinglink.common.UuidFactory
import felix.livinglink.common.addSampleEventSourcingEvents
import felix.livinglink.common.addSampleGroups
import felix.livinglink.common.addSampleUsers
import felix.livinglink.common.assertHasTotalRecords
import felix.livinglink.common.defaultAppModule
import felix.livinglink.common.delete
import felix.livinglink.common.get
import felix.livinglink.common.loginUser
import felix.livinglink.common.registerUser
import felix.livinglink.eventSourcing.GetEventSourcingEventsResponse
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

        // Arrange
        val uuidFactory: UuidFactory = object : UuidFactory {
            var callCount = 0

            override fun invoke(): String {
                return "uuid$callCount".also { callCount++ }
            }
        }

        application {
            module(
                config = config,
                appModule = defaultAppModule(
                    config = config,
                    uuidFactory = uuidFactory
                )
            )
        }

        database.addSampleUsers(user = TestData.alice)
        database.addSampleUsers(user = TestData.bob)
        database.addSampleGroups(group = TestData.groupAliceAndBob)
        database.addSampleEventSourcingEvents(events = TestData.eventsGroupFromAliceAndBob)

        val loginResponseAlice = client.loginUser(
            username = TestData.alice.username,
            password = TestData.alice.password
        )

        // Act
        val deleteResponse: DeleteUserResponse = client.delete(
            urlString = "auth/account",
            token = loginResponseAlice.accessToken
        )

        // Assert
        assertEquals(DeleteUserResponse.Success, deleteResponse)
        database.assertHasTotalRecords(UsersTable, totalRecords = 1)

        val loginResponseBob = client.loginUser(
            username = TestData.bob.username,
            password = TestData.bob.password
        )

        val remainingEvents: GetEventSourcingEventsResponse = client.get(
            urlString = "eventSourcing/events?groupId=${TestData.groupAliceAndBob.id}",
            token = loginResponseBob.accessToken
        )

        remainingEvents.events.forEach { event ->
            assert(event.userId != TestData.alice.id) {
                "Expected all events to be anonymized, but found one with original userId: ${event.userId}"
            }
        }

        assertRedisChangeSet(userId = TestData.alice.id, expectedChangeId = "uuid2")
        assertRedisChangeSet(userId = TestData.bob.id, expectedChangeId = "uuid3")
        assertRedisChangeSet(groupId = TestData.groupAliceAndBob.id, expectedEventId = 3)
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