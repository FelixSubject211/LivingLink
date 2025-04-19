package felix.livinglink.groups

import felix.livinglink.common.BaseIntegrationTest
import felix.livinglink.common.UuidFactory
import felix.livinglink.common.addSampleGroups
import felix.livinglink.common.addSampleUsers
import felix.livinglink.common.defaultAppModule
import felix.livinglink.common.delete
import felix.livinglink.common.get
import felix.livinglink.common.loginUser
import felix.livinglink.group.DeleteGroupResponse
import felix.livinglink.group.GetGroupsForUserResponse
import felix.livinglink.module
import io.ktor.server.testing.testApplication
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlin.test.Test

class DeleteGroupTest: BaseIntegrationTest() {

    @Test
    fun `should delete group if user is member`() = testApplication {
        val groupToDelete = TestData.groupOwnedByAlice1
        val uuid = "any-uuid"

        // Arrange
        val uuidFactory = object : UuidFactory {
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

        database.addSampleGroups(groups = listOf(groupToDelete))

        val token = client.loginUser(TestData.alice.username, TestData.alice.password).accessToken

        // Act
        val deleteResponse: DeleteGroupResponse = client.delete(
            urlString = "groups/${groupToDelete.id}",
            token = token
        )

        // Assert
        assertEquals(DeleteGroupResponse.Success, deleteResponse)

        val getGroups: GetGroupsForUserResponse = client.get("groups", token)
        assertTrue(getGroups.groups.none { it.id == groupToDelete.id })

        assertRedisChangeSet(userId = TestData.alice.id, expectedChangeId = uuid)
    }

    @Test
    fun `should return NotAllowed if user is not a member of the group`() = testApplication {
        val groupToDelete = TestData.groupOwnedByBob

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

        database.addSampleGroups(groups = listOf(groupToDelete))

        val token = client.loginUser(TestData.alice.username, TestData.alice.password).accessToken

        val deleteResponse: DeleteGroupResponse = client.delete(
            urlString = "groups/${groupToDelete.id}",
            token = token
        )

        assertEquals(DeleteGroupResponse.NotAllowed, deleteResponse)

        val getGroups: GetGroupsForUserResponse = client.get("groups", token)
        assertTrue(getGroups.groups.none { it.id == groupToDelete.id })

        assertNoRedisChangeSet(userId = TestData.alice.id)
        assertNoRedisChangeSet(userId = TestData.bob.id)
    }
}