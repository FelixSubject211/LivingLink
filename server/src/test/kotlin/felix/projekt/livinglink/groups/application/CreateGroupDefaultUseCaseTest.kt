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
import felix.projekt.livinglink.server.groups.application.CreateGroupDefaultUseCase
import felix.projekt.livinglink.server.groups.domain.Group
import felix.projekt.livinglink.server.groups.domain.GroupRepository
import felix.projekt.livinglink.server.groups.domain.GroupVersionCache
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CreateGroupDefaultUseCaseTest {
    private lateinit var mockGroupRepository: GroupRepository
    private lateinit var mockGroupVersionCache: GroupVersionCache
    private lateinit var sut: CreateGroupDefaultUseCase

    private val baseGroup = Group(
        id = "group-123",
        name = "MyGroup",
        memberIdToMember = emptyMap(),
        inviteCodeIdToInviteCode = emptyMap(),
        version = 0L,
    )

    @BeforeTest
    fun setup() {
        mockGroupRepository = mock(mode = MockMode.autofill)
        mockGroupVersionCache = mock(mode = MockMode.autofill)
        sut = CreateGroupDefaultUseCase(
            groupRepository = mockGroupRepository,
            groupVersionCache = mockGroupVersionCache,
        )
    }

    @Test
    fun `creates group, adds member and updates cache`() = runTest {
        // Arrange
        val userId = "user-1"
        val username = "Felix"
        val groupName = "Study Group"

        val createdGroup = baseGroup.copy(id = "new-group-id", name = groupName)
        val updatedGroup = createdGroup.addMember(userId, username)

        val lambdaCapture = lambdaCapture<GroupRepository.UpdateOperationResult<Group, Unit>>()

        everySuspend { mockGroupRepository.createGroup(groupName) } returns createdGroup
        everySuspend {
            mockGroupRepository.updateWithOptimisticLocking(
                groupId = createdGroup.id,
                maxRetries = any(),
                update = capture(lambdaCapture),
            )
        } returns GroupRepository.UpdateResult(
            entity = updatedGroup,
            response = Unit,
        )

        // Act
        val result = sut.invoke(userId, username, groupName)

        // Assert
        assertEquals(updatedGroup, result)

        val lambda = lambdaCapture.values.first()
        val operationResult = lambda(createdGroup)
        assertIs<GroupRepository.UpdateOperationResult.Updated<Group, *>>(operationResult)
        assertTrue(
            operationResult.newEntity.memberIdToMember.containsKey(userId),
            "Lambda should add the given user to the group",
        )

        verifySuspend(exhaustiveOrder) {
            mockGroupRepository.createGroup(groupName)
            mockGroupRepository.updateWithOptimisticLocking<Unit>(
                groupId = createdGroup.id,
                maxRetries = any(),
                update = any(),
            )
            mockGroupVersionCache.addOrUpdateGroupVersionIfUserExists(
                userId = userId,
                groupId = createdGroup.id,
                version = updatedGroup.version,
            )
        }

        verifyNoMoreCalls(mockGroupRepository, mockGroupVersionCache)
    }
}