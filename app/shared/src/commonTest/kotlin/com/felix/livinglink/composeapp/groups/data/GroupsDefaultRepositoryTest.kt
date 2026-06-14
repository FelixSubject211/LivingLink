package com.felix.livinglink.composeapp.groups.data

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.felix.livinglink.composeapp.auth.domain.AuthRepository
import com.felix.livinglink.composeapp.auth.domain.AuthState
import com.felix.livinglink.composeapp.core.domain.Loadable
import com.felix.livinglink.composeapp.core.domain.NetworkResult
import com.felix.livinglink.composeapp.groups.domain.Group
import com.felix.livinglink.composeapp.groups.domain.GroupsContent
import com.felix.livinglink.composeapp.groups.domain.GroupsRemoteDataSource
import dev.mokkery.answering.returns
import dev.mokkery.answering.sequentially
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.exactly
import dev.mokkery.verifySuspend
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
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
    private lateinit var authRepository: AuthRepository

    private lateinit var repository: GroupsDefaultRepository

    @BeforeTest
    fun setUp() {
        remoteDataSource = mock()
        authRepository = mock()
    }

    private fun TestScope.createRepository() {
        repository = GroupsDefaultRepository(
            groupsRemoteDataSource = remoteDataSource,
            authRepository = authRepository,
            scope = backgroundScope,
        )
    }

    private fun stubLoggedIn() {
        every { authRepository.authState } returns MutableStateFlow(
            AuthState.LoggedIn(apiKey = "key", userId = "user-1", username = "felix"),
        )
    }

    private suspend fun ReceiveTurbine<Loadable<GroupsContent>>.awaitNonLoading(): Loadable<GroupsContent> {
        while (true) {
            val item = awaitItem()
            if (item != Loadable.Loading) return item
        }
    }

    @Test
    fun `state is loading when not logged in`() = runTest {
        every { authRepository.authState } returns MutableStateFlow(AuthState.LoggedOut)

        createRepository()

        repository.state.test {
            assertEquals(Loadable.Loading, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `state emits content with first group selected by default`() = runTest {
        val groups = listOf(Group(id = "g1", name = "A"), Group(id = "g2", name = "B"))

        stubLoggedIn()
        everySuspend { remoteDataSource.getGroups(any()) } returns NetworkResult.Success(groups)

        createRepository()

        repository.state.test {
            assertEquals(
                Loadable.Content(
                    GroupsContent(groups = groups, selectedGroup = groups.first()),
                ),
                awaitNonLoading(),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `state emits empty when no groups`() = runTest {
        stubLoggedIn()
        everySuspend { remoteDataSource.getGroups(any()) } returns NetworkResult.Success(emptyList())

        createRepository()

        repository.state.test {
            assertEquals(Loadable.Empty, awaitNonLoading())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `state emits network error when load fails and nothing cached`() = runTest {
        stubLoggedIn()
        everySuspend { remoteDataSource.getGroups(any()) } returns NetworkResult.NetworkError

        createRepository()

        repository.state.test {
            assertEquals(Loadable.Error.Network, awaitNonLoading())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selectGroup changes the selected group in state`() = runTest {
        val groups = listOf(Group(id = "g1", name = "A"), Group(id = "g2", name = "B"))

        stubLoggedIn()
        everySuspend { remoteDataSource.getGroups(any()) } returns NetworkResult.Success(groups)

        createRepository()

        repository.state.test {
            assertEquals("g1", (awaitNonLoading() as Loadable.Content).value.selectedGroup.id)

            repository.selectGroup("g2")

            assertEquals("g2", (awaitItem() as Loadable.Content).value.selectedGroup.id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selectedGroupId maps to the effective selected group id`() = runTest {
        val groups = listOf(Group(id = "g1", name = "A"), Group(id = "g2", name = "B"))

        stubLoggedIn()
        everySuspend { remoteDataSource.getGroups(any()) } returns NetworkResult.Success(groups)

        createRepository()

        repository.selectedGroupId.test {
            assertEquals("g1", awaitItemSkippingNull())

            repository.selectGroup("g2")

            assertEquals("g2", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selectedGroupId is null while loading`() = runTest {
        every { authRepository.authState } returns MutableStateFlow(AuthState.LoggedOut)

        createRepository()

        assertNull(repository.selectedGroupId.first())
    }

    @Test
    fun `shareIn keeps a single upstream for multiple state collectors`() = runTest {
        val groups = listOf(Group(id = "g1", name = "A"))

        stubLoggedIn()
        everySuspend { remoteDataSource.getGroups(any()) } returns NetworkResult.Success(groups)

        createRepository()

        repository.state.test {
            awaitNonLoading()

            repository.selectedGroupId.test {
                awaitItemSkippingNull()
                cancelAndIgnoreRemainingEvents()
            }

            cancelAndIgnoreRemainingEvents()
        }

        verifySuspend(exactly(1)) { remoteDataSource.getGroups(any()) }
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

        repository.state.test {
            assertEquals("A v1", (awaitNonLoading() as Loadable.Content).value.groups.first().name)

            advanceTimeBy(61_000)
            runCurrent()

            assertEquals("A v2", (awaitItem() as Loadable.Content).value.groups.first().name)
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

    private suspend fun ReceiveTurbine<String?>.awaitItemSkippingNull(): String {
        while (true) {
            val item = awaitItem()
            if (item != null) return item
        }
    }
}