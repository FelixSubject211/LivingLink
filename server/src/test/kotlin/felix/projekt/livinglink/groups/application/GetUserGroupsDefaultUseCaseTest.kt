package felix.projekt.livinglink.groups.application

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.exhaustiveOrder
import dev.mokkery.verifyNoMoreCalls
import dev.mokkery.verifySuspend
import felix.projekt.livinglink.server.groups.application.GetUserGroupsDefaultUseCase
import felix.projekt.livinglink.server.groups.domain.GetGroupsResponse
import felix.projekt.livinglink.server.groups.domain.Group
import felix.projekt.livinglink.server.groups.domain.GroupRepository
import felix.projekt.livinglink.server.groups.domain.GroupVersionCache
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GetUserGroupsDefaultUseCaseTest {
    private lateinit var mockGroupRepository: GroupRepository
    private lateinit var mockGroupVersionCache: GroupVersionCache
    private lateinit var sut: GetUserGroupsDefaultUseCase

    private val group1 = Group(
        id = "group-1",
        name = "Family",
        memberIdToMember = mapOf("user-1" to Group.Member("user-1", "Felix")),
        inviteCodeIdToInviteCode = emptyMap(),
        version = 5L,
    )

    private val group2 = Group(
        id = "group-2",
        name = "Work",
        memberIdToMember = mapOf("user-1" to Group.Member("user-1", "Felix")),
        inviteCodeIdToInviteCode = emptyMap(),
        version = 7L,
    )

    @BeforeTest
    fun setup() {
        mockGroupRepository = mock(mode = MockMode.autofill)
        mockGroupVersionCache = mock(mode = MockMode.autofill)
        sut = GetUserGroupsDefaultUseCase(
            groupRepository = mockGroupRepository,
            groupVersionCache = mockGroupVersionCache,
        )
    }

    @Test
    fun `returns NotModified when cache matches current versions`() = runTest {
        // Arrange
        val userId = "user-1"
        val currentVersions = mapOf("group-1" to 5L, "group-2" to 7L)
        val cachedVersions = GroupVersionCache.GroupVersions(currentVersions)

        everySuspend { mockGroupVersionCache.getGroupVersions(userId) } returns cachedVersions

        // Act
        val result = sut.invoke(userId, currentVersions)

        // Assert
        assertIs<GetGroupsResponse.NotModified>(result)

        verifySuspend(exhaustiveOrder) {
            mockGroupVersionCache.getGroupVersions(userId)
        }

        verifyNoMoreCalls(mockGroupRepository, mockGroupVersionCache)
    }

    @Test
    fun `returns Success and updates cache when versions differ`() = runTest {
        // Arrange
        val userId = "user-1"
        val currentVersions = mapOf("group-1" to 4L)
        val cachedVersions = GroupVersionCache.GroupVersions(mapOf("group-1" to 3L))
        val groups = mapOf(group1.id to group1, group2.id to group2)
        val newVersions = GroupVersionCache.GroupVersions(
            mapOf("group-1" to 5L, "group-2" to 7L),
        )

        everySuspend { mockGroupVersionCache.getGroupVersions(userId) } returns cachedVersions
        everySuspend { mockGroupRepository.getGroupsForMember(userId) } returns groups

        // Act
        val result = sut.invoke(userId, currentVersions)

        // Assert
        assertIs<GetGroupsResponse.Success>(result)
        assertEquals(groups, result.groups)

        verifySuspend(exhaustiveOrder) {
            mockGroupVersionCache.getGroupVersions(userId)
            mockGroupRepository.getGroupsForMember(userId)
            mockGroupVersionCache.setGroupVersions(userId, newVersions)
        }

        verifyNoMoreCalls(mockGroupRepository, mockGroupVersionCache)
    }

    @Test
    fun `returns NotModified after fetching if new versions still match current`() = runTest {
        // Arrange
        val userId = "user-1"
        val currentVersions = mapOf("group-1" to 5L)
        val cachedVersions = GroupVersionCache.GroupVersions(mapOf("group-1" to 3L))
        val groups = mapOf(group1.id to group1)
        val newVersions = GroupVersionCache.GroupVersions(mapOf("group-1" to 5L))

        everySuspend { mockGroupVersionCache.getGroupVersions(userId) } returns cachedVersions
        everySuspend { mockGroupRepository.getGroupsForMember(userId) } returns groups

        // Act
        val result = sut.invoke(userId, currentVersions)

        // Assert
        assertIs<GetGroupsResponse.NotModified>(result)

        verifySuspend(exhaustiveOrder) {
            mockGroupVersionCache.getGroupVersions(userId)
            mockGroupRepository.getGroupsForMember(userId)
            mockGroupVersionCache.setGroupVersions(userId, newVersions)
        }

        verifyNoMoreCalls(mockGroupRepository, mockGroupVersionCache)
    }
}