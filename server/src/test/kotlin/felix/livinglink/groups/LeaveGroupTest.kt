package felix.livinglink.groups

import felix.livinglink.common.BaseIntegrationTest
import felix.livinglink.common.TestData
import felix.livinglink.common.UuidFactory
import felix.livinglink.common.addSampleGroups
import felix.livinglink.common.addSampleUsers
import felix.livinglink.common.defaultAppModule
import felix.livinglink.common.get
import felix.livinglink.common.loginUser
import felix.livinglink.common.post
import felix.livinglink.group.GetGroupsForUserResponse
import felix.livinglink.group.LeaveGroupRequest
import felix.livinglink.group.LeaveGroupResponse
import felix.livinglink.module
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LeaveGroupTest : BaseIntegrationTest() {

    @Test
    fun `should leave group if user is member`() = testApplication {
        val group = TestData.groupAliceAndBob
        val uuid = "any-uuid"

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
            users = listOf(TestData.alice, TestData.bob),
            refreshTokens = listOf(TestData.refreshTokenAlice)
        )
        database.addSampleGroups(groups = listOf(group))

        val token = client.loginUser(TestData.alice.username, TestData.alice.password).accessToken

        val response: LeaveGroupResponse = client.post(
            urlString = "groups/leave",
            token = token,
            request = LeaveGroupRequest(groupId = group.id)
        )

        assertEquals(LeaveGroupResponse.Success, response)

        val groupsAfter: GetGroupsForUserResponse = client.get("groups/get", token)
        assertTrue(groupsAfter.groups.none { it.id == group.id })

        assertRedisChangeSet(userId = TestData.alice.id, expectedChangeId = uuid)
    }

    @Test
    fun `should return LastAdminCannotLeave if user is sole admin`() = testApplication {
        val group = TestData.groupOwnedByAlice1.copy(
            groupMemberIdsToName = mapOf(
                TestData.alice.id to TestData.alice.username,
                TestData.bob.id to TestData.bob.username
            ),
            adminUserIds = setOf(TestData.alice.id)
        )

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

        val response: LeaveGroupResponse = client.post(
            urlString = "groups/leave",
            token = token,
            request = LeaveGroupRequest(groupId = group.id)
        )

        assertEquals(LeaveGroupResponse.LastAdminCannotLeave, response)
    }

    @Test
    fun `should return NotAllowed if user not member`() = testApplication {
        val group = TestData.groupOwnedByBob

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

        val response: LeaveGroupResponse = client.post(
            urlString = "groups/leave",
            token = token,
            request = LeaveGroupRequest(groupId = group.id)
        )

        assertEquals(LeaveGroupResponse.NotAllowed, response)

        val groupsAfter: GetGroupsForUserResponse = client.get("groups/get", token)
        assertTrue(groupsAfter.groups.none { it.id == group.id })

        assertNoRedisChangeSet(userId = TestData.alice.id)
        assertNoRedisChangeSet(userId = TestData.bob.id)
    }
}
