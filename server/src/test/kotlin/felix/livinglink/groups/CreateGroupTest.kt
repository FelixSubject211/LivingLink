package felix.livinglink.groups

import felix.livinglink.common.*
import felix.livinglink.group.CreateGroupRequest
import felix.livinglink.group.CreateGroupResponse
import felix.livinglink.group.GetGroupsForUserResponse
import felix.livinglink.module
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CreateGroupTest : BaseIntegrationTest() {

    @Test
    fun `should create group and return groupId, user becomes member`() = testApplication {
        val groupName = "GroupName"

        // Arrange
        application {
            module(
                config = config,
                appModule = defaultAppModule(config = config)
            )
        }

        database.addSampleUsers(
            users = listOf(TestData.alice),
            refreshTokens = listOf(TestData.refreshTokenAlice)
        )

        val token = client.loginUser(TestData.alice.username, TestData.alice.password).accessToken

        // Act
        val createResponse: CreateGroupResponse = client.post(
            urlString = "groups/create",
            token = token,
            request = CreateGroupRequest(groupName = groupName)
        )

        // Assert
        require(createResponse is CreateGroupResponse.Success)
        assertTrue(createResponse.groupId.isNotBlank(), "Group ID should not be blank")


        val getGroups: GetGroupsForUserResponse = client.get(
            urlString = "groups",
            token = token
        )

        val createdGroup = getGroups.groups.firstOrNull { it.id == createResponse.groupId }
        requireNotNull(createdGroup) { "Group not returned in list" }
        assertEquals(groupName, createdGroup.name)
        assertEquals(TestData.alice.username, createdGroup.groupMemberIdsToName[TestData.alice.id])
    }
}