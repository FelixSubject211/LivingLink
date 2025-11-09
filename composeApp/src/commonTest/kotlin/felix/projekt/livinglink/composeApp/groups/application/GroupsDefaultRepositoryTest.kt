package felix.projekt.livinglink.composeApp.groups.application

import app.cash.turbine.test
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.exhaustiveOrder
import dev.mokkery.verifyNoMoreCalls
import dev.mokkery.verifySuspend
import felix.projekt.livinglink.composeApp.auth.interfaces.GetAuthStateService
import felix.projekt.livinglink.composeApp.core.domain.Result
import felix.projekt.livinglink.composeApp.groups.domain.GetGroupsResponse
import felix.projekt.livinglink.composeApp.groups.domain.Group
import felix.projekt.livinglink.composeApp.groups.domain.GroupsNetworkDataSource
import felix.projekt.livinglink.composeApp.groups.domain.GroupsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GroupsDefaultRepositoryTest {

    private lateinit var mockGroupsNetworkDataSource: GroupsNetworkDataSource
    private lateinit var mockGetAuthStateService: GetAuthStateService
    private lateinit var getAuthStateServiceFlow: MutableSharedFlow<GetAuthStateService.AuthState>
    private lateinit var sut: GroupsDefaultRepository
    private lateinit var testScope: CoroutineScope
    val group1 = Group(
        id = "1",
        name = "Group1",
        memberIdToMember = emptyMap(),
        inviteCodes = emptyList(),
        version = 0
    )
    val group2 = Group(
        id = "2",
        name = "Group2",
        memberIdToMember = emptyMap(),
        inviteCodes = emptyList(),
        version = 1
    )

    @BeforeTest
    fun setup() {
        testScope = CoroutineScope(UnconfinedTestDispatcher())

        mockGroupsNetworkDataSource = mock(mode = MockMode.autofill)
        mockGetAuthStateService = mock(mode = MockMode.autofill)
        getAuthStateServiceFlow = MutableSharedFlow()

        every { mockGetAuthStateService() } returns getAuthStateServiceFlow

        sut = GroupsDefaultRepository(
            groupsNetworkDataSource = mockGroupsNetworkDataSource,
            getAuthStateService = mockGetAuthStateService,
            scope = testScope
        )
    }

    @Test
    fun `polling starts only when flow is collected`() = runTest(UnconfinedTestDispatcher()) {
        val groupResponse = mapOf(group1.id to group1, group2.id to group2)

        getAuthStateServiceFlow.emit(GetAuthStateService.AuthState.LoggedIn)

        everySuspend { mockGroupsNetworkDataSource.getGroups(any()) } returns Result.Success(
            GetGroupsResponse.Success(
                groups = groupResponse,
                nextPollAfterMillis = 1000L
            )
        )

        sut.getGroups.test {
            val expected = GroupsRepository.GroupsRepositoryState.Data(groupResponse)
            assertEquals(expected, awaitItem())
        }
    }

    @Test
    fun `polling stops 5 seconds after flow collection stops`() = runTest {
        var callCount = 0
        val groupResponse = mapOf(group1.id to group1)

        everySuspend { mockGroupsNetworkDataSource.getGroups(any()) } returns Result.Success(
            GetGroupsResponse.Success(
                groups = groupResponse,
                nextPollAfterMillis = 1000L
            )
        ).also { callCount++ }

        val job = launch {
            sut.getGroups.test {
                val expected = GroupsRepository.GroupsRepositoryState.Data(groupResponse)
                assertEquals(expected, awaitItem())
            }
        }

        job.cancel()

        advanceTimeBy(4000)
        assertTrue(callCount == 1, "Polling should not be stopped")

        advanceTimeBy(2000)
        assertTrue(callCount == 1, "Polling should be stopped")
    }

    @Test
    fun `UserLoggedIn triggers polling`() = runTest(UnconfinedTestDispatcher()) {
        var callCount = 0

        everySuspend { mockGroupsNetworkDataSource.getGroups(any()) } returns Result.Success(
            GetGroupsResponse.Success(
                groups = mapOf(group1.id to group1),
                nextPollAfterMillis = 1000L
            )
        ).also { callCount++ }

        val job = launch { sut.getGroups.test { cancelAndConsumeRemainingEvents() } }
        getAuthStateServiceFlow.emit(GetAuthStateService.AuthState.LoggedIn)
        advanceTimeBy(2000)
        assertTrue(callCount > 0)
        job.cancel()
    }

    @Test
    fun `UserLoggedOut stops polling and clears groups`() = runTest(UnconfinedTestDispatcher()) {
        val groupResponse = mapOf(group1.id to group1)

        getAuthStateServiceFlow.emit(GetAuthStateService.AuthState.LoggedIn)

        everySuspend { mockGroupsNetworkDataSource.getGroups(any()) } returns Result.Success(
            GetGroupsResponse.Success(
                groups = groupResponse,
                nextPollAfterMillis = 1000L
            )
        )

        sut.getGroups.test {
            val expected1 = GroupsRepository.GroupsRepositoryState.Data(groupResponse)
            assertEquals(expected1, awaitItem())

            getAuthStateServiceFlow.emit(GetAuthStateService.AuthState.LoggedOut)
            val expected2 = GroupsRepository.GroupsRepositoryState.Loading
            assertEquals(expected2, awaitItem())

            getAuthStateServiceFlow.emit(GetAuthStateService.AuthState.LoggedIn)
            val expected3 = GroupsRepository.GroupsRepositoryState.Data(groupResponse)
            assertEquals(expected3, awaitItem())
        }
    }

    @Test
    fun `multiple collectors share same polling flow`() = runTest(UnconfinedTestDispatcher()) {
        val groupResponse = mapOf(group1.id to group1)

        everySuspend { mockGroupsNetworkDataSource.getGroups(any()) } returns Result.Success(
            GetGroupsResponse.Success(
                groups = groupResponse,
                nextPollAfterMillis = 1000L
            )
        )

        getAuthStateServiceFlow.emit(GetAuthStateService.AuthState.LoggedIn)

        val job1 = launch { sut.getGroups.test { awaitItem() } }
        val job2 = launch { sut.getGroups.test { awaitItem() } }

        advanceTimeBy(3000)

        job1.cancel()
        job2.cancel()

        verifySuspend(exhaustiveOrder) {
            mockGroupsNetworkDataSource.getGroups(any())
        }
        verifyNoMoreCalls(mockGroupsNetworkDataSource)
    }
}