package felix.projekt.livinglink.groups.application

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.capture
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.exhaustiveOrder
import dev.mokkery.verifyNoMoreCalls
import dev.mokkery.verifySuspend
import felix.projekt.common.lambdaCapture
import felix.projekt.livinglink.server.groups.application.JoinGroupWithInviteCodeDefaultUseCase
import felix.projekt.livinglink.server.groups.domain.Group
import felix.projekt.livinglink.server.groups.domain.GroupRepository
import felix.projekt.livinglink.server.groups.domain.GroupVersionCache
import felix.projekt.livinglink.server.groups.domain.JoinGroupResponse
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

class JoinGroupWithInviteCodeDefaultUseCaseTest {
    private lateinit var mockGroupRepository: GroupRepository
    private lateinit var mockGroupVersionCache: GroupVersionCache
    private lateinit var sut: JoinGroupWithInviteCodeDefaultUseCase

    private val inviteCodeId = "invite-123"
    private val inviteCodeKey = "TESTKEY"
    private val testGroup = Group(
        id = "group-123",
        name = "TestGroup",
        memberIdToMember = mapOf(
            "user-1" to Group.Member(id = "user-1", username = "Felix")
        ),
        inviteCodeIdToInviteCode = mapOf(
            inviteCodeId to Group.InviteCode(
                id = inviteCodeId,
                key = inviteCodeKey,
                name = "Invite",
                creatorId = "user-1",
                usages = 1
            )
        ),
        version = 2L
    )

    @BeforeTest
    fun setup() {
        mockGroupRepository = mock(mode = MockMode.autofill)
        mockGroupVersionCache = mock(mode = MockMode.autofill)
        sut = JoinGroupWithInviteCodeDefaultUseCase(
            groupRepository = mockGroupRepository,
            groupVersionCache = mockGroupVersionCache
        )
    }

    @Test
    fun `adds member increments invite code usage and updates cache`() = runTest {
        // Arrange
        val userId = "user-2"
        val username = "Lisa"
        val lambdaCapture = lambdaCapture<GroupRepository.UpdateOperationResult<Group, JoinGroupResponse>>()

        everySuspend { mockGroupRepository.getGroupByInviteCodeKey(inviteCodeKey) } returns testGroup

        everySuspend { mockGroupRepository.getInviteCodeIdByKey(inviteCodeKey) } returns inviteCodeId

        val updatedGroup = testGroup
            .addMember(userId = userId, username = username)
            .incrementInviteCodeUsage(inviteCodeId)
            .copy(version = testGroup.version + 1)

        everySuspend {
            mockGroupRepository.updateWithOptimisticLocking(
                groupId = testGroup.id,
                maxRetries = any(),
                update = capture(lambdaCapture)
            )
        } returns GroupRepository.UpdateResult(
            entity = updatedGroup,
            response = JoinGroupResponse.Success(updatedGroup)
        )

        // Act
        val result = sut.invoke(userId, username, inviteCodeKey)

        // Assert
        assertIs<JoinGroupResponse.Success>(result)
        assertTrue(result.group.memberIdToMember.containsKey(userId))

        val lambda = lambdaCapture.values.first()
        val operationResult = lambda(testGroup)
        assertIs<GroupRepository.UpdateOperationResult.Updated<Group, *>>(operationResult)

        val lambdaGroup = operationResult.newEntity
        assertTrue(lambdaGroup.memberIdToMember.containsKey(userId))
        assertTrue(
            lambdaGroup.inviteCodeIdToInviteCode[inviteCodeId]?.usages == testGroup.inviteCodeIdToInviteCode[inviteCodeId]?.usages?.plus(
                1
            )
        )

        verifySuspend(exhaustiveOrder) {
            mockGroupRepository.getGroupByInviteCodeKey(inviteCodeKey)
            mockGroupRepository.updateWithOptimisticLocking<JoinGroupResponse>(
                groupId = testGroup.id,
                maxRetries = any(),
                update = any()
            )
            mockGroupVersionCache.addOrUpdateGroupVersionIfUserExists(
                userId = "user-1",
                groupId = updatedGroup.id,
                version = updatedGroup.version
            )
            mockGroupVersionCache.addOrUpdateGroupVersionIfUserExists(
                userId = userId,
                groupId = updatedGroup.id,
                version = updatedGroup.version
            )
            mockGroupRepository.getInviteCodeIdByKey(inviteCodeKey)
        }

        verifyNoMoreCalls(mockGroupRepository, mockGroupVersionCache)
    }
}
