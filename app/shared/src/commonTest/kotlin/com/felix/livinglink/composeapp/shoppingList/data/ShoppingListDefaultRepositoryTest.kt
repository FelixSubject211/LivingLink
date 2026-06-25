package com.felix.livinglink.composeapp.shoppingList.data

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.felix.livinglink.composeapp.auth.domain.AuthRepository
import com.felix.livinglink.composeapp.auth.domain.AuthState
import com.felix.livinglink.composeapp.core.domain.Loadable
import com.felix.livinglink.composeapp.core.domain.NetworkResult
import com.felix.livinglink.composeapp.groups.domain.Group
import com.felix.livinglink.composeapp.groups.domain.GroupsContent
import com.felix.livinglink.composeapp.groups.domain.GroupsRepository
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListContent
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListItem
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListLocalDataSource
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListPage
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListRemoteDataSource
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListRemoteDataSource.ChangeItemResult
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListRepository
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.eq
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verify.VerifyMode.Companion.atLeast
import dev.mokkery.verify.VerifyMode.Companion.exactly
import dev.mokkery.verifySuspend
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class ShoppingListDefaultRepositoryTest {

    private lateinit var remoteDataSource: ShoppingListRemoteDataSource
    private lateinit var localDataSource: ShoppingListLocalDataSource
    private lateinit var authRepository: AuthRepository
    private lateinit var groupsRepository: GroupsRepository

    private lateinit var repository: ShoppingListDefaultRepository

    @BeforeTest
    fun setUp() {
        remoteDataSource = mock()
        localDataSource = mock()
        authRepository = mock()
        groupsRepository = mock()
    }

    private fun TestScope.createRepository() {
        repository = ShoppingListDefaultRepository(
            shoppingListRemoteDataSource = remoteDataSource,
            shoppingListLocalDataSource = localDataSource,
            authRepository = authRepository,
            groupsRepository = groupsRepository,
            dispatcher = UnconfinedTestDispatcher(testScheduler),
        )
    }

    private fun groupsContent(
        groups: List<Group>,
        selected: Group = groups.first(),
    ) = Loadable.Content(
        GroupsContent(groups = groups, selectedGroup = selected),
    )

    private fun stubLoggedIn() {
        every { authRepository.authState } returns MutableStateFlow(
            AuthState.LoggedIn(apiKey = "key", userId = "user-1", username = "felix"),
        )
    }

    @Test
    fun `emits loading while groups are loading`() = runTest {
        every { groupsRepository.state } returns flowOf(Loadable.Loading)
        every { localDataSource.observe(any()) } returns MutableStateFlow(null)

        createRepository()

        repository.state.test {
            assertEquals(Loadable.Loading, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits network error when groups fail to load`() = runTest {
        every { groupsRepository.state } returns flowOf(Loadable.Error.Network)
        every { localDataSource.observe(any()) } returns MutableStateFlow(null)

        createRepository()

        repository.state.test {
            assertEquals(Loadable.Error.Network, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits empty when user has no groups`() = runTest {
        every { groupsRepository.state } returns flowOf(Loadable.Empty)
        every { localDataSource.observe(any()) } returns MutableStateFlow(null)

        createRepository()

        repository.state.test {
            assertEquals(Loadable.Empty, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `fills cache from network, then serves from cache on re-subscription`() = runTest {
        val groupId = "group-1"
        val group = Group(id = groupId, name = "A")

        val item = ShoppingListItem(
            id = "item-1",
            name = "Milk",
            completed = false,
            createdByUserId = "user-1",
            createdAt = Instant.fromEpochMilliseconds(0),
            updatedAt = Instant.fromEpochMilliseconds(0),
        )
        val cachedContent = ShoppingListContent(
            itemsById = mapOf(item.id to item),
            order = listOf(item.id),
        )

        val cacheFlow = MutableStateFlow<ShoppingListContent?>(null)

        every { groupsRepository.state } returns flowOf(groupsContent(groups = listOf(group)))
        every { authRepository.authState } returns MutableStateFlow(
            AuthState.LoggedIn(apiKey = "key", userId = "user-1", username = "felix"),
        )
        every { localDataSource.observe(groupId) } returns cacheFlow

        everySuspend {
            localDataSource.putRange(any(), any(), any(), any())
        } calls { cacheFlow.value = cachedContent }

        everySuspend {
            remoteDataSource.getPage(any(), any(), any(), any(), any())
        } returns NetworkResult.Success(
            ShoppingListPage(items = listOf(item), nextCursor = null, totalCount = 1),
        )

        createRepository()

        repository.state.test {
            assertEquals(Loadable.Loading, awaitItem())
            assertEquals(Loadable.Content(cachedContent), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        verifySuspend(VerifyMode.atLeast(1)) {
            localDataSource.putRange(any(), any(), any(), any())
        }

        everySuspend {
            remoteDataSource.getPage(any(), any(), any(), any(), any())
        } returns NetworkResult.NetworkError

        repository.state.test {
            assertEquals(Loadable.Content(cachedContent), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loads correct pages when scrolling down and back up`() = runTest {
        val groupId = "group-1"
        val group = Group(id = groupId, name = "A")
        val totalCount = 1000

        fun itemAt(index: Int) = ShoppingListItem(
            id = "item-$index",
            name = "Name $index",
            completed = false,
            createdByUserId = "user-1",
            createdAt = Instant.fromEpochMilliseconds(0),
            updatedAt = Instant.fromEpochMilliseconds(0),
        )

        val stubCache = StubMergingCache()

        every { groupsRepository.state } returns flowOf(groupsContent(groups = listOf(group)))
        every { authRepository.authState } returns MutableStateFlow(
            AuthState.LoggedIn(apiKey = "key", userId = "user-1", username = "felix"),
        )
        every { localDataSource.observe(groupId) } returns stubCache.flow

        everySuspend {
            remoteDataSource.getPage(any(), any(), any(), any(), any())
        } calls { args ->
            val limit = args.component4<Int>()
            val cursor = args.component5<String>()
            val from = cursor.toInt()
            val items = (from until (from + limit)).map(::itemAt)
            NetworkResult.Success(
                ShoppingListPage(items = items, nextCursor = null, totalCount = totalCount),
            )
        }

        everySuspend {
            localDataSource.putRange(any(), any(), any(), any())
        } calls { args ->
            stubCache.put(
                fromIndex = args.component2<Int>(),
                items = args.component3<List<ShoppingListItem>>(),
                totalCount = args.component4<Int>(),
            )
        }

        createRepository()

        repository.setVisibleRange(firstVisibleIndex = 400, lastVisibleIndex = 450)

        repository.state.test {
            val afterScrollDown = awaitItemMatching { it.itemAt(400) != null }
            assertEquals(itemAt(400), afterScrollDown.itemAt(400))
            assertEquals(itemAt(450), afterScrollDown.itemAt(450))

            repository.setVisibleRange(firstVisibleIndex = 0, lastVisibleIndex = 50)

            val afterScrollUp = awaitItemMatching { it.itemAt(0) != null }
            assertEquals(itemAt(0), afterScrollUp.itemAt(0))
            assertEquals(itemAt(50), afterScrollUp.itemAt(50))

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `polling refreshes the currently visible pages with fresh data`() = runTest {
        val groupId = "group-1"
        val group = Group(id = groupId, name = "A")
        val totalCount = 1000

        fun itemAt(index: Int, version: String) = ShoppingListItem(
            id = "item-$index",
            name = "Name $index $version",
            completed = false,
            createdByUserId = "user-1",
            createdAt = Instant.fromEpochMilliseconds(0),
            updatedAt = Instant.fromEpochMilliseconds(0),
        )

        var version = "v1"
        val stubCache = StubMergingCache()

        every { groupsRepository.state } returns flowOf(groupsContent(groups = listOf(group)))
        every { authRepository.authState } returns MutableStateFlow(
            AuthState.LoggedIn(apiKey = "key", userId = "user-1", username = "felix"),
        )
        every { localDataSource.observe(groupId) } returns stubCache.flow

        everySuspend {
            remoteDataSource.getPage(any(), any(), any(), any(), any())
        } calls { args ->
            val limit = args.component4<Int>()
            val cursor = args.component5<String>()
            val from = cursor.toInt()
            val items = (from until (from + limit)).map { itemAt(it, version) }
            NetworkResult.Success(
                ShoppingListPage(items = items, nextCursor = null, totalCount = totalCount),
            )
        }

        everySuspend {
            localDataSource.putRange(any(), any(), any(), any())
        } calls { args ->
            stubCache.put(
                fromIndex = args.component2<Int>(),
                items = args.component3<List<ShoppingListItem>>(),
                totalCount = args.component4<Int>(),
            )
        }

        createRepository()

        repository.state.test {
            assertEquals(Loadable.Loading, awaitItem())

            repository.setVisibleRange(firstVisibleIndex = 400, lastVisibleIndex = 450)
            runCurrent()

            val firstLoad = awaitItemMatching { it.itemAt(400)?.name == "Name 400 v1" }
            assertEquals(itemAt(400, "v1"), firstLoad.itemAt(400))

            version = "v2"
            advanceTimeBy(6000)
            runCurrent()

            val afterPoll = awaitItemMatching { it.itemAt(400)?.name == "Name 400 v2" }
            assertEquals(itemAt(400, "v2"), afterPoll.itemAt(400))
            assertEquals(itemAt(450, "v2"), afterPoll.itemAt(450))

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `changeItemCompleteState writes server item to cache on success`() = runTest {
        val groupId = "group-1"
        val serverItem = ShoppingListItem(
            id = "item-1",
            name = "Milk",
            completed = true,
            createdByUserId = "user-1",
            createdAt = Instant.fromEpochMilliseconds(0),
            updatedAt = Instant.fromEpochMilliseconds(0),
        )

        stubLoggedIn()
        every { groupsRepository.selectedGroupId } returns flowOf(groupId)
        everySuspend {
            remoteDataSource.changeItemCompleteState(any(), any(), any(), any())
        } returns NetworkResult.Success(ChangeItemResult.Updated(serverItem))
        everySuspend {
            localDataSource.updateItem(any(), any(), any())
        } returns Unit

        createRepository()

        val result = repository.changeItemCompleteState(itemId = "item-1", completed = true)

        assertEquals(ShoppingListRepository.ChangeCompleteStateResult.Success, result)
        verifySuspend(exactly(1)) {
            remoteDataSource.changeItemCompleteState("key", groupId, "item-1", true)
        }
        verifySuspend(exactly(1)) {
            localDataSource.updateItem(groupId, "item-1", any())
        }
    }

    @Test
    fun `changeItemCompleteState removes item from cache when server reports not found`() = runTest {
        val groupId = "group-1"

        stubLoggedIn()
        every { groupsRepository.selectedGroupId } returns flowOf(groupId)
        everySuspend {
            remoteDataSource.changeItemCompleteState(any(), any(), any(), any())
        } returns NetworkResult.Success(ChangeItemResult.NotFound)
        everySuspend {
            localDataSource.removeItem(any(), any())
        } returns Unit

        createRepository()

        val result = repository.changeItemCompleteState(itemId = "item-1", completed = true)

        assertEquals(ShoppingListRepository.ChangeCompleteStateResult.Success, result)
        verifySuspend(exactly(1)) {
            localDataSource.removeItem(groupId, "item-1")
        }
        verifySuspend(exactly(0)) {
            localDataSource.updateItem(any(), any(), any())
        }
    }

    @Test
    fun `changeItemCompleteState on conflict returns Conflict and triggers page reload`() = runTest {
        val groupId = "group-1"
        val group = Group(id = groupId, name = "A")

        val staleItem = ShoppingListItem(
            id = "item-1",
            name = "Milk",
            completed = false,
            createdByUserId = "user-1",
            createdAt = Instant.fromEpochMilliseconds(0),
            updatedAt = Instant.fromEpochMilliseconds(0),
        )
        val freshItem = staleItem.copy(completed = true)

        val cacheFlow = MutableStateFlow<ShoppingListContent?>(
            ShoppingListContent(
                itemsById = mapOf("item-1" to staleItem),
                order = listOf("item-1"),
            ),
        )

        every { groupsRepository.state } returns flowOf(groupsContent(groups = listOf(group)))
        every { groupsRepository.selectedGroupId } returns flowOf(groupId)
        every { authRepository.authState } returns MutableStateFlow(
            AuthState.LoggedIn(apiKey = "key", userId = "user-1", username = "felix"),
        )
        every { localDataSource.observe(groupId) } returns cacheFlow

        everySuspend {
            remoteDataSource.changeItemCompleteState(any(), any(), any(), any())
        } returns NetworkResult.Success(ChangeItemResult.Conflict)

        everySuspend {
            remoteDataSource.getPage(any(), any(), any(), any(), any())
        } returns NetworkResult.Success(
            ShoppingListPage(items = listOf(freshItem), nextCursor = null, totalCount = 1),
        )
        everySuspend {
            localDataSource.putRange(any(), any(), any(), any())
        } calls {
            cacheFlow.value = ShoppingListContent(
                itemsById = mapOf("item-1" to freshItem),
                order = listOf("item-1"),
            )
        }

        createRepository()

        repository.state.test {
            awaitItem()

            val result = repository.changeItemCompleteState(itemId = "item-1", completed = true)
            assertEquals(ShoppingListRepository.ChangeCompleteStateResult.Conflict, result)

            awaitItemMatching { it.itemAt(0)?.completed == true }

            cancelAndIgnoreRemainingEvents()
        }

        verifySuspend(atLeast(1)) {
            remoteDataSource.getPage("key", groupId, any(), 200, "0")
        }
    }

    @Test
    fun `changeItemCompleteState returns NetworkError and leaves cache untouched`() = runTest {
        val groupId = "group-1"

        stubLoggedIn()
        every { groupsRepository.selectedGroupId } returns flowOf(groupId)
        everySuspend {
            remoteDataSource.changeItemCompleteState(any(), any(), any(), any())
        } returns NetworkResult.NetworkError

        createRepository()

        val result = repository.changeItemCompleteState(itemId = "item-1", completed = true)

        assertEquals(ShoppingListRepository.ChangeCompleteStateResult.NetworkError, result)
        verifySuspend(exactly(0)) {
            localDataSource.updateItem(any(), any(), any())
        }
    }

    @Test
    fun `changeItemCompleteState clears auth and returns NoActiveGroup on unauthorized`() = runTest {
        val groupId = "group-1"

        stubLoggedIn()
        every { groupsRepository.selectedGroupId } returns flowOf(groupId)
        everySuspend { authRepository.clear() } returns Unit
        everySuspend {
            remoteDataSource.changeItemCompleteState(any(), any(), any(), any())
        } returns NetworkResult.Unauthorized

        createRepository()

        val result = repository.changeItemCompleteState(itemId = "item-1", completed = true)

        assertEquals(ShoppingListRepository.ChangeCompleteStateResult.NoActiveGroup, result)
        verifySuspend(exactly(1)) { authRepository.clear() }
    }

    @Test
    fun `changeItemCompleteState returns NoActiveGroup when not logged in`() = runTest {
        every { authRepository.authState } returns MutableStateFlow(AuthState.LoggedOut)

        createRepository()

        val result = repository.changeItemCompleteState(itemId = "item-1", completed = true)

        assertEquals(ShoppingListRepository.ChangeCompleteStateResult.NoActiveGroup, result)
        verifySuspend(exactly(0)) {
            remoteDataSource.changeItemCompleteState(any(), any(), any(), any())
        }
    }

    @Test
    fun `changeItemCompleteState returns NoActiveGroup when no group selected`() = runTest {
        stubLoggedIn()
        every { groupsRepository.selectedGroupId } returns flowOf(null)

        createRepository()

        val result = repository.changeItemCompleteState(itemId = "item-1", completed = true)

        assertEquals(ShoppingListRepository.ChangeCompleteStateResult.NoActiveGroup, result)
        verifySuspend(exactly(0)) {
            remoteDataSource.changeItemCompleteState(any(), any(), any(), any())
        }
    }

    private suspend fun ReceiveTurbine<Loadable<ShoppingListContent>>.awaitItemMatching(
        predicate: (ShoppingListContent) -> Boolean,
    ): ShoppingListContent {
        while (true) {
            val item = awaitItem()
            if (item is Loadable.Content && predicate(item.value)) return item.value
        }
    }

    @Test
    fun `deleteItem removes item from cache on success`() = runTest {
        val groupId = "group-1"

        stubLoggedIn()
        every { groupsRepository.selectedGroupId } returns flowOf(groupId)
        everySuspend {
            remoteDataSource.deleteItem(any(), any(), any())
        } returns NetworkResult.Success(true)
        everySuspend {
            localDataSource.removeItem(any(), any())
        } returns Unit

        createRepository()

        val result = repository.deleteItem(itemId = "item-1")

        assertEquals(ShoppingListRepository.DeleteResult.Success, result)
        verifySuspend(exactly(1)) {
            remoteDataSource.deleteItem("key", groupId, "item-1")
        }
        verifySuspend(exactly(1)) {
            localDataSource.removeItem(groupId, "item-1")
        }
    }

    @Test
    fun `deleteItem removes item from cache even when server reports not found`() = runTest {
        val groupId = "group-1"

        stubLoggedIn()
        every { groupsRepository.selectedGroupId } returns flowOf(groupId)
        everySuspend {
            remoteDataSource.deleteItem(any(), any(), any())
        } returns NetworkResult.Success(false)
        everySuspend {
            localDataSource.removeItem(any(), any())
        } returns Unit

        createRepository()

        val result = repository.deleteItem(itemId = "item-1")

        assertEquals(ShoppingListRepository.DeleteResult.Success, result)
        verifySuspend(exactly(1)) {
            localDataSource.removeItem(groupId, "item-1")
        }
    }

    @Test
    fun `deleteItem returns NetworkError and leaves cache untouched`() = runTest {
        val groupId = "group-1"

        stubLoggedIn()
        every { groupsRepository.selectedGroupId } returns flowOf(groupId)
        everySuspend {
            remoteDataSource.deleteItem(any(), any(), any())
        } returns NetworkResult.NetworkError

        createRepository()

        val result = repository.deleteItem(itemId = "item-1")

        assertEquals(ShoppingListRepository.DeleteResult.NetworkError, result)
        verifySuspend(exactly(0)) {
            localDataSource.removeItem(any(), any())
        }
    }

    @Test
    fun `deleteItem clears auth and returns NoActiveGroup on unauthorized`() = runTest {
        val groupId = "group-1"

        stubLoggedIn()
        every { groupsRepository.selectedGroupId } returns flowOf(groupId)
        everySuspend { authRepository.clear() } returns Unit
        everySuspend {
            remoteDataSource.deleteItem(any(), any(), any())
        } returns NetworkResult.Unauthorized

        createRepository()

        val result = repository.deleteItem(itemId = "item-1")

        assertEquals(ShoppingListRepository.DeleteResult.NoActiveGroup, result)
        verifySuspend(exactly(1)) { authRepository.clear() }
        verifySuspend(exactly(0)) {
            localDataSource.removeItem(any(), any())
        }
    }

    @Test
    fun `deleteItem returns NoActiveGroup when not logged in`() = runTest {
        every { authRepository.authState } returns MutableStateFlow(AuthState.LoggedOut)

        createRepository()

        val result = repository.deleteItem(itemId = "item-1")

        assertEquals(ShoppingListRepository.DeleteResult.NoActiveGroup, result)
        verifySuspend(exactly(0)) {
            remoteDataSource.deleteItem(any(), any(), any())
        }
    }

    @Test
    fun `deleteItem returns NoActiveGroup when no group selected`() = runTest {
        stubLoggedIn()
        every { groupsRepository.selectedGroupId } returns flowOf(null)

        createRepository()

        val result = repository.deleteItem(itemId = "item-1")

        assertEquals(ShoppingListRepository.DeleteResult.NoActiveGroup, result)
        verifySuspend(exactly(0)) {
            remoteDataSource.deleteItem(any(), any(), any())
        }
    }
}

private class StubMergingCache {
    val flow = MutableStateFlow<ShoppingListContent?>(null)

    fun put(fromIndex: Int, items: List<ShoppingListItem>, totalCount: Int) {
        flow.value = stubMerge(flow.value, fromIndex, items, totalCount)
    }

    private fun stubMerge(
        current: ShoppingListContent?,
        fromIndex: Int,
        items: List<ShoppingListItem>,
        totalCount: Int,
    ): ShoppingListContent {
        val freshIds = items.mapTo(mutableSetOf()) { it.id }
        val freshRange = fromIndex until (fromIndex + items.size)

        val order = List(totalCount) { index ->
            when {
                index in freshRange -> items[index - fromIndex].id
                else -> current?.order?.getOrNull(index)?.takeUnless { it in freshIds }
            }
        }
        val idsInOrder = order.filterNotNullTo(mutableSetOf())
        val itemsById = buildMap {
            current?.itemsById?.forEach { (id, item) ->
                if (id in idsInOrder) put(id, item)
            }
            items.forEach { put(it.id, it) }
        }
        return ShoppingListContent(itemsById = itemsById, order = order)
    }
}