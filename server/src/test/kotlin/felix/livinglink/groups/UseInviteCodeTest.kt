package felix.livinglink.groups

import felix.livinglink.common.*
import felix.livinglink.group.CreateInviteRequest
import felix.livinglink.group.GetGroupsForUserResponse
import felix.livinglink.group.UseInviteRequest
import felix.livinglink.group.UseInviteResponse
import felix.livinglink.module
import io.ktor.server.testing.testApplication
import org.ktorm.dsl.from
import org.ktorm.dsl.select
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class UseInviteCodeTest : BaseIntegrationTest() {

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `should allow invited user to join group and remove code`() = testApplication {
        val group = TestData.groupOwnedByAlice1
        val code = Uuid.random().toString()

        // Arrange
        val uuidFactory = object : UuidFactory {
            override fun invoke(): String = code
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

        val aliceToken = client.loginUser(TestData.alice.username, TestData.alice.password).accessToken
        val bobToken = client.loginUser(TestData.bob.username, TestData.bob.password).accessToken

        // Act
        val inviteResponse = client.post<CreateInviteRequest, felix.livinglink.group.CreateInviteResponse>(
            urlString = "groups/invite/create",
            token = aliceToken,
            request = CreateInviteRequest(groupId = group.id)
        )

        // Assert
        assertEquals(code.take(16), inviteResponse.code)

        val useResponse = client.post<UseInviteRequest, UseInviteResponse>(
            urlString = "groups/invite/use",
            token = bobToken,
            request = UseInviteRequest(code = inviteResponse.code)
        )
        assertEquals(UseInviteResponse.Success, useResponse)

        val updatedGroups = client.get<GetGroupsForUserResponse>(
            urlString = "groups",
            token = bobToken
        )
        assertTrue(updatedGroups.groups.any {
            it.id == group.id && it.groupMemberIdsToName.containsKey(TestData.bob.id)
        })

        val remainingInvites = database.from(GroupInvitesTable).select().totalRecordsInAllPages
        assertEquals(0, remainingInvites)
    }

    @Test
    fun `should fail to use invite if code does not exist`() = testApplication {

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
        val response = client.post<UseInviteRequest, UseInviteResponse>(
            urlString = "groups/invite/use",
            token = token,
            request = UseInviteRequest(code = "non-existent-code")
        )

        // Assert
        assertEquals(UseInviteResponse.InvalidOrAlreadyUsed, response)
    }
}
