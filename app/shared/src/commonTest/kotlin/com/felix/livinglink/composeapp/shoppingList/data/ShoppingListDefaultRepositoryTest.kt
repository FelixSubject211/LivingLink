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
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
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

    @Test
    fun `retains present groups and evicts removed ones, ignoring non-content states`() = runTest {
        val groupA = Group(id = "group-1", name = "A")
        val groupB = Group(id = "group-2", name = "B")

        every { groupsRepository.state } returns flowOf(
            Loadable.Content(
                GroupsContent(
                    groups = listOf(groupA, groupB),
                    selectedGroup = groupA,
                ),
            ),
            Loadable.Content(
                GroupsContent(
                    groups = listOf(groupA),
                    selectedGroup = groupA,
                ),
            ),
            Loadable.Loading,
        )
        every { groupsRepository.selectedGroupId } returns flowOf(null)
        everySuspend { localDataSource.retainGroups(any()) } returns Unit

        createRepository()

        repository.state.test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        verifySuspend(VerifyMode.exhaustiveOrder) {
            localDataSource.retainGroups(setOf("group-1", "group-2"))
            localDataSource.retainGroups(setOf("group-1"))
        }
    }

    @Test
    fun `fills cache from network, then serves from cache on re-subscription`() = runTest {
        val groupId = "group-1"

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

        every { groupsRepository.state } returns flowOf(Loadable.Loading)
        every { groupsRepository.selectedGroupId } returns flowOf(groupId)
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

        every { groupsRepository.state } returns flowOf(Loadable.Loading)
        every { groupsRepository.selectedGroupId } returns flowOf(groupId)
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

        every { groupsRepository.state } returns flowOf(Loadable.Loading)
        every { groupsRepository.selectedGroupId } returns flowOf(groupId)
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

    private suspend fun ReceiveTurbine<Loadable<ShoppingListContent>>.awaitItemMatching(
        predicate: (ShoppingListContent) -> Boolean,
    ): ShoppingListContent {
        while (true) {
            val item = awaitItem()
            if (item is Loadable.Content && predicate(item.value)) return item.value
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