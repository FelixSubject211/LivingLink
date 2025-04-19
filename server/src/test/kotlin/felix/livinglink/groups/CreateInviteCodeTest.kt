package felix.livinglink.groups

import felix.livinglink.common.BaseIntegrationTest
import felix.livinglink.common.UuidFactory
import felix.livinglink.common.addSampleGroups
import felix.livinglink.common.addSampleUsers
import felix.livinglink.common.defaultAppModule
import felix.livinglink.common.loginUser
import felix.livinglink.common.post
import felix.livinglink.group.CreateInviteRequest
import felix.livinglink.group.CreateInviteResponse
import felix.livinglink.module
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class CreateInviteCodeTest : BaseIntegrationTest() {

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `should create invite code if user is member of group`() = testApplication {
        val group = TestData.groupOwnedByAlice1
        val uuid = Uuid.random().toString()

        // Arrange
        val uuidFactory: UuidFactory = object : UuidFactory {
            override fun invoke() = uuid
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

        database.addSampleUsers(
            users = listOf(TestData.alice),
            refreshTokens = listOf(TestData.refreshTokenAlice)
        )
        database.addSampleGroups(groups = listOf(group))

        val token = client.loginUser(TestData.alice.username, TestData.alice.password).accessToken

        // Act
        val response: CreateInviteResponse = client.post(
            urlString = "groups/invite/create",
            token = token,
            request = CreateInviteRequest(groupId = group.id)
        )

        // Assert
        assertNotNull(response.code)
        assertEquals(uuid.take(16), response.code)

        assertNoRedisChangeSet(userId = TestData.alice.id)
    }

    @Test
    fun `should fail if user is not in group`() = testApplication {
        val group = TestData.groupOwnedByBob

        // Arrange
        application {
            module(
                config = config,
                appModule = defaultAppModule(config = config)
            )
        }

        database.addSampleUsers(
            users = listOf(TestData.alice, TestData.bob),
            refreshTokens = listOf(TestData.refreshTokenAlice)
        )
        database.addSampleGroups(groups = listOf(group))

        val token = client.loginUser(TestData.alice.username, TestData.alice.password).accessToken

        // Act
        // Assert
        assertThrows<IllegalArgumentException> {
            client.post(
                urlString = "groups/invite/create",
                token = token,
                request = CreateInviteRequest(groupId = group.id)
            )
        }

        assertNoRedisChangeSet(userId = TestData.alice.id)
        assertNoRedisChangeSet(userId = TestData.bob.id)
    }
}