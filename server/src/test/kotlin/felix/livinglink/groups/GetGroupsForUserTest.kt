package felix.livinglink.groups

import felix.livinglink.common.BaseIntegrationTest
import felix.livinglink.common.addSampleGroups
import felix.livinglink.common.addSampleUsers
import felix.livinglink.common.defaultAppModule
import felix.livinglink.common.get
import felix.livinglink.common.loginUser
import felix.livinglink.group.GetGroupsForUserResponse
import felix.livinglink.module
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class GetGroupsForUserTest : BaseIntegrationTest() {

    @Test
    fun `should return only groups where the user is a member`() = testApplication {
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

        database.addSampleGroups(
            groups = listOf(
                TestData.groupOwnedByAlice1,
                TestData.groupOwnedByAlice2,
                TestData.groupOwnedByBob
            )
        )

        // Act
        val response: GetGroupsForUserResponse = client.get(
            urlString = "groups/get",
            token = client.loginUser(TestData.alice.username, TestData.alice.password).accessToken
        )

        // Assert
        assertEquals(
            expected = setOf(TestData.groupOwnedByAlice1, TestData.groupOwnedByAlice2),
            actual = response.groups
        )

        assertNoRedisChangeSet(userId = TestData.alice.id)
        assertNoRedisChangeSet(userId = TestData.bob.id)
    }
}