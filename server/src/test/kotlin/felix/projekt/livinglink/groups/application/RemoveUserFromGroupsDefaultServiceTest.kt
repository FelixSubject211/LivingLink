package felix.projekt.livinglink.groups.application

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.capture
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.exhaustiveOrder
import dev.mokkery.verifyNoMoreCalls
import dev.mokkery.verifySuspend
import felix.projekt.livinglink.server.groups.application.RemoveUserFromGroupsDefaultService
import felix.projekt.livinglink.server.groups.domain.Group
import felix.projekt.livinglink.server.groups.domain.GroupRepository
import felix.projekt.livinglink.server.groups.domain.GroupVersionCache
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertIs

class RemoveUserFromGroupsDefaultServiceTest {
    private lateinit var mockGroupRepository: GroupRepository
    private lateinit var mockGroupVersionCache: GroupVersionCache
    private lateinit var sut: RemoveUserFromGroupsDefaultService

    private val group1 = Group(
        id = "group-1",
        name = "Group One",
        memberIdToMember = mapOf(
            "user-1" to Group.Member("user-1", "Felix"),
            "user-2" to Group.Member("user-2", "Lisa"),
        ),
        inviteCodeIdToInviteCode = emptyMap(),
        version = 1L,
    )

    private val group2 = Group(
        id = "group-2",
        name = "Solo Group",
        memberIdToMember = mapOf("user-1" to Group.Member("user-1", "Felix")),
        inviteCodeIdToInviteCode = emptyMap(),
        version = 3L,
    )

    @BeforeTest
    fun setup() {
        mockGroupRepository = mock(mode = MockMode.autofill)
        mockGroupVersionCache = mock(mode = MockMode.autofill)
        sut = RemoveUserFromGroupsDefaultService(
            groupRepository = mockGroupRepository,
            groupVersionCache = mockGroupVersionCache,
        )
    }

    @Test
    fun `removes user from multi-member groups, deletes single-member groups and updates cache`() = runTest {
        // Arrange
        val userId = "user-1"
        val allGroups = mapOf(group1.id to group1, group2.id to group2)

        val updatedGroup1 = group1.removeMember(userId)
        val lambdaCapture = lambdaCapture<GroupRepository.UpdateOperationResult<Group, Unit>>()

        every { mockGroupRepository.getGroupsForMember(userId) } returns allGroups
        everySuspend {
            mockGroupRepository.updateWithOptimisticLocking(
                groupId = group1.id,
                maxRetries = any(),
                update = capture(lambdaCapture),
            )
        } returns GroupRepository.UpdateResult(entity = updatedGroup1, response = Unit)

        everySuspend { mockGroupRepository.deleteGroup(group2.id) } returns Unit

        // Act
        sut.invoke(userId)

        // Assert
        val lambda = lambdaCapture.values.first()
        val result = lambda(group1)
        assertIs<GroupRepository.UpdateOperationResult.Updated<Group, *>>(result)
        assertFalse(
            result.newEntity.memberIdToMember.containsKey(userId),
            "Lambda should remove the given user from the group",
        )

        verifySuspend(exhaustiveOrder) {
            mockGroupRepository.getGroupsForMember(userId)
            mockGroupRepository.updateWithOptimisticLocking<Unit>(
                groupId = group1.id,
                maxRetries = any(),
                update = any(),
            )
            mockGroupVersionCache.addOrUpdateGroupVersionIfUserExists(
                userId = "user-2",
                groupId = group1.id,
                version = updatedGroup1.version,
            )
            mockGroupRepository.deleteGroup(group2.id)
            mockGroupVersionCache.deleteGroupVersions(userId)
        }

        verifyNoMoreCalls(mockGroupRepository, mockGroupVersionCache)
    }
}