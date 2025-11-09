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
import felix.projekt.livinglink.server.groups.application.DeleteInviteCodeDefaultUseCase
import felix.projekt.livinglink.server.groups.domain.DeleteInviteCodeResponse
import felix.projekt.livinglink.server.groups.domain.Group
import felix.projekt.livinglink.server.groups.domain.GroupRepository
import felix.projekt.livinglink.server.groups.domain.GroupVersionCache
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DeleteInviteCodeDefaultUseCaseTest {
    private lateinit var mockGroupRepository: GroupRepository
    private lateinit var mockGroupVersionCache: GroupVersionCache
    private lateinit var sut: DeleteInviteCodeDefaultUseCase

    private val baseGroup = Group(
        id = "group-1",
        name = "Test Group",
        memberIdToMember = mapOf(
            "user-1" to Group.Member("user-1", "Felix"),
            "user-2" to Group.Member("user-2", "Lisa"),
        ),
        inviteCodeIdToInviteCode = mapOf(
            "invite-123" to Group.InviteCode(
                id = "invite-123",
                key = "ABC123",
                name = "Test Invite",
                creatorId = "user-1",
                usages = 0,
            )
        ),
        version = 2L,
    )

    @BeforeTest
    fun setup() {
        mockGroupRepository = mock(mode = MockMode.autofill)
        mockGroupVersionCache = mock(mode = MockMode.autofill)
        sut = DeleteInviteCodeDefaultUseCase(
            groupRepository = mockGroupRepository,
            groupVersionCache = mockGroupVersionCache,
        )
    }

    @Test
    fun `removes invite code and updates cache for all members`() = runTest {
        // Arrange
        val userId = "user-1"
        val groupId = baseGroup.id
        val inviteCodeId = "invite-123"

        val updatedGroup = baseGroup.removeInviteCode(inviteCodeId)
        val lambdaCapture = lambdaCapture<GroupRepository.UpdateOperationResult<Group, DeleteInviteCodeResponse>>()

        everySuspend {
            mockGroupRepository.updateWithOptimisticLocking(
                groupId = groupId,
                maxRetries = any(),
                update = capture(lambdaCapture),
            )
        } returns GroupRepository.UpdateResult(
            entity = updatedGroup,
            response = DeleteInviteCodeResponse.Success,
        )

        // Act
        val result = sut.invoke(userId, groupId, inviteCodeId)

        // Assert
        assertIs<DeleteInviteCodeResponse.Success>(result)

        val lambda = lambdaCapture.values.first()
        val operationResult = lambda(baseGroup)
        assertIs<GroupRepository.UpdateOperationResult.Updated<Group, *>>(operationResult)
        assertTrue(
            operationResult.newEntity.inviteCodeIdToInviteCode[inviteCodeId] == null,
            "Lambda should remove the invite code from the group",
        )

        verifySuspend(exhaustiveOrder) {
            mockGroupRepository.updateWithOptimisticLocking<DeleteInviteCodeResponse>(
                groupId = groupId,
                maxRetries = any(),
                update = any(),
            )
            mockGroupVersionCache.addOrUpdateGroupVersionIfUserExists(
                userId = "user-1",
                groupId = groupId,
                version = updatedGroup.version,
            )
            mockGroupVersionCache.addOrUpdateGroupVersionIfUserExists(
                userId = "user-2",
                groupId = groupId,
                version = updatedGroup.version,
            )
        }

        verifyNoMoreCalls(mockGroupRepository, mockGroupVersionCache)
    }
}