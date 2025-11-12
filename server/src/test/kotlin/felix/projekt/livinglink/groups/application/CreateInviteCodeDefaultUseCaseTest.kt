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
import felix.projekt.livinglink.server.groups.application.CreateInviteCodeDefaultUseCase
import felix.projekt.livinglink.server.groups.domain.CreateInviteCodeResponse
import felix.projekt.livinglink.server.groups.domain.Group
import felix.projekt.livinglink.server.groups.domain.GroupRepository
import felix.projekt.livinglink.server.groups.domain.GroupVersionCache
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CreateInviteCodeDefaultUseCaseTest {
    private lateinit var mockGroupRepository: GroupRepository
    private lateinit var mockGroupVersionCache: GroupVersionCache
    private lateinit var sut: CreateInviteCodeDefaultUseCase

    private val testGroup = Group(
        id = "group-123",
        name = "TestGroup",
        memberIdToMember = mapOf(
            "user-1" to Group.Member(id = "user-1", username = "Felix"),
            "user-2" to Group.Member(id = "user-2", username = "Lisa")
        ),
        inviteCodeIdToInviteCode = emptyMap(),
        version = 1L
    )

    @BeforeTest
    fun setup() {
        mockGroupRepository = mock(mode = MockMode.autofill)
        mockGroupVersionCache = mock(mode = MockMode.autofill)
        sut = CreateInviteCodeDefaultUseCase(
            groupRepository = mockGroupRepository,
            groupVersionCache = mockGroupVersionCache,
            uuidProvider = { "uuid-123" }
        )
    }

    @Test
    fun `creates invite code and updates group cache for all members`() = runTest {
        // Arrange
        val userId = "user-1"
        val groupId = testGroup.id
        val inviteCodeName = "Invite"
        val lambdaCapture = lambdaCapture<GroupRepository.UpdateOperationResult<Group, CreateInviteCodeResponse>>()

        everySuspend {
            mockGroupRepository.updateWithOptimisticLocking(
                groupId = groupId,
                maxRetries = any(),
                update = capture(lambdaCapture)
            )
        } returns GroupRepository.UpdateResult(
            entity = testGroup,
            response = CreateInviteCodeResponse.Success("TESTKEY")
        )

        // Act
        val result = sut.invoke(userId, groupId, inviteCodeName)

        // Assert
        assertIs<CreateInviteCodeResponse.Success>(result)
        assertTrue(result.key.isNotBlank())

        val lambda = lambdaCapture.values.first()
        val operationResult = lambda(testGroup)
        assertIs<GroupRepository.UpdateOperationResult.Updated<Group, *>>(operationResult)

        val updated = operationResult.newEntity
        assertTrue(
            updated.inviteCodeIdToInviteCode.values.any {
                it.name == inviteCodeName && it.creatorId == userId
            },
            "Lambda should add a new invite code with correct data"
        )

        verifySuspend(exhaustiveOrder) {
            mockGroupRepository.updateWithOptimisticLocking<CreateInviteCodeResponse>(
                groupId = groupId,
                maxRetries = any(),
                update = any()
            )
            mockGroupVersionCache.addOrUpdateGroupVersionIfUserExists(
                userId = "user-1",
                groupId = groupId,
                version = testGroup.version
            )
            mockGroupVersionCache.addOrUpdateGroupVersionIfUserExists(
                userId = "user-2",
                groupId = groupId,
                version = testGroup.version
            )
        }

        verifyNoMoreCalls(mockGroupRepository, mockGroupVersionCache)
    }
}