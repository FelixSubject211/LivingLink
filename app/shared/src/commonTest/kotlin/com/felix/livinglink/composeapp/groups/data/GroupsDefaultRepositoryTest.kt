package com.felix.livinglink.composeapp.groups.data

import app.cash.turbine.test
import com.felix.livinglink.composeapp.auth.domain.AuthRepository
import com.felix.livinglink.composeapp.auth.domain.AuthState
import com.felix.livinglink.composeapp.core.domain.Loadable
import com.felix.livinglink.composeapp.core.domain.NetworkResult
import com.felix.livinglink.composeapp.groups.domain.Group
import com.felix.livinglink.composeapp.groups.domain.GroupsContent
import com.felix.livinglink.composeapp.groups.domain.GroupsLocalDataSource
import com.felix.livinglink.composeapp.groups.domain.GroupsRemoteDataSource
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.answering.sequentially
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.exactly
import dev.mokkery.verifySuspend
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class GroupsDefaultRepositoryTest {

    private lateinit var remoteDataSource: GroupsRemoteDataSource
    private lateinit var localDataSource: GroupsLocalDataSource
    private lateinit var authRepository: AuthRepository

    private lateinit var repository: GroupsDefaultRepository

    private val cachedGroups = MutableStateFlow<List<Group>?>(null)
    private val selectedGroupId = MutableStateFlow<String?>(null)

    @BeforeTest
    fun setUp() {
        remoteDataSource = mock()
        authRepository = mock()
        localDataSource = mock()

        every { localDataSource.observe() } returns cachedGroups
        every { localDataSource.observeSelectedGroupId() } returns selectedGroupId
        everySuspend { localDataSource.replaceAll(any()) } calls { args ->
            cachedGroups.value = args.component1<List<Group>>()
        }
        everySuspend { localDataSource.setSelectedGroupId(any()) } calls { args ->
            selectedGroupId.value = args.component1<String>()
        }
    }

    private fun TestScope.createRepository() {
        repository = GroupsDefaultRepository(
            groupsRemoteDataSource = remoteDataSource,
            groupsLocalDataSource = localDataSource,
            authRepository = authRepository,
            scope = backgroundScope,
        )
    }

    private fun stubLoggedIn() {
        every { authRepository.authState } returns MutableStateFlow(
            AuthState.LoggedIn(apiKey = "key", userId = "user-1", username = "felix"),
        )
    }

    private val Flow<Loadable<GroupsContent>>.contents: Flow<GroupsContent>
        get() = filterIsInstance<Loadable.Content<GroupsContent>>().map { it.value }

    @Test
    fun `state is loading when not logged in and nothing cached`() = runTest {
        every { authRepository.authState } returns MutableStateFlow(AuthState.LoggedOut)

        createRepository()

        repository.state.test {
            assertEquals(Loadable.Loading, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `state shows cached groups offline even when network fails`() = runTest {
        val groups = listOf(Group(id = "g1", name = "A"), Group(id = "g2", name = "B"))

        cachedGroups.value = groups

        stubLoggedIn()
        everySuspend { remoteDataSource.getGroups(any()) } returns NetworkResult.NetworkError

        createRepository()

        assertEquals(
            GroupsContent(groups = groups, selectedGroup = groups.first()),
            repository.state.contents.first(),
        )
    }

    @Test
    fun `state emits content with first group selected by default`() = runTest {
        val groups = listOf(Group(id = "g1", name = "A"), Group(id = "g2", name = "B"))

        stubLoggedIn()
        everySuspend { remoteDataSource.getGroups(any()) } returns NetworkResult.Success(groups)

        createRepository()

        assertEquals(
            GroupsContent(groups = groups, selectedGroup = groups.first()),
            repository.state.contents.first(),
        )
    }

    @Test
    fun `state emits empty when no groups`() = runTest {
        stubLoggedIn()
        everySuspend { remoteDataSource.getGroups(any()) } returns NetworkResult.Success(emptyList())
        everySuspend { localDataSource.replaceAll(any()) } calls {
            cachedGroups.value = emptyList()
        }

        createRepository()

        assertEquals(Loadable.Empty, repository.state.first { it != Loadable.Loading })
    }

    @Test
    fun `state emits network error when load fails and nothing cached`() = runTest {
        stubLoggedIn()
        everySuspend { remoteDataSource.getGroups(any()) } returns NetworkResult.NetworkError

        createRepository()

        assertEquals(Loadable.Error.Network, repository.state.first { it != Loadable.Loading })
    }

    @Test
    fun `selectGroup changes the selected group in state`() = runTest {
        val groups = listOf(Group(id = "g1", name = "A"), Group(id = "g2", name = "B"))

        stubLoggedIn()
        everySuspend { remoteDataSource.getGroups(any()) } returns NetworkResult.Success(groups)

        createRepository()

        val selectedIds = repository.state.contents.map { it.selectedGroup.id }

        assertEquals("g1", selectedIds.first())

        repository.selectGroup("g2")

        assertEquals("g2", selectedIds.first { it == "g2" })
    }

    @Test
    fun `selectedGroupId maps to the effective selected group id`() = runTest {
        val groups = listOf(Group(id = "g1", name = "A"), Group(id = "g2", name = "B"))

        stubLoggedIn()
        everySuspend { remoteDataSource.getGroups(any()) } returns NetworkResult.Success(groups)

        createRepository()

        assertEquals("g1", repository.selectedGroupId.filterNotNull().first())

        repository.selectGroup("g2")

        assertEquals("g2", repository.selectedGroupId.first { it == "g2" })
    }

    @Test
    fun `selectedGroupId is null while loading`() = runTest {
        every { authRepository.authState } returns MutableStateFlow(AuthState.LoggedOut)

        createRepository()

        assertNull(repository.selectedGroupId.first())
    }

    @Test
    fun `polling reloads groups after the poll interval`() = runTest {
        val v1 = listOf(Group(id = "g1", name = "A v1"))
        val v2 = listOf(Group(id = "g1", name = "A v2"))

        stubLoggedIn()
        everySuspend { remoteDataSource.getGroups(any()) } sequentially {
            returns(NetworkResult.Success(v1))
            returns(NetworkResult.Success(v2))
        }

        createRepository()

        val firstNames = repository.state.contents
            .map { it.groups.first().name }
            .distinctUntilChanged()

        firstNames.test {
            assertEquals("A v1", awaitItem())

            advanceTimeBy(61_000)
            runCurrent()

            assertEquals("A v2", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `unauthorized clears auth`() = runTest {
        stubLoggedIn()
        everySuspend { authRepository.clear() } returns Unit
        everySuspend { remoteDataSource.getGroups(any()) } returns NetworkResult.Unauthorized

        createRepository()

        repository.state.test {
            assertEquals(Loadable.Loading, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        verifySuspend(exactly(1)) { authRepository.clear() }
    }
}