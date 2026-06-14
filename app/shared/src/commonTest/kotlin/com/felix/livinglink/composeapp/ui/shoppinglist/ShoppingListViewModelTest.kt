package com.felix.livinglink.composeapp.ui.shoppinglist

import app.cash.turbine.test
import com.felix.livinglink.composeapp.core.domain.Loadable
import com.felix.livinglink.composeapp.shoppingList.application.ChangeShoppingListItemCompleteStateUseCase
import com.felix.livinglink.composeapp.shoppingList.application.ObserveShoppingListUseCase
import com.felix.livinglink.composeapp.shoppingList.application.SetShoppingListVisibleRangeUseCase
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListContent
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListItem
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListRepository
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.exactly
import dev.mokkery.verifySuspend
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class ShoppingListViewModelTest {

    private lateinit var setVisibleRangeUseCase: SetShoppingListVisibleRangeUseCase
    private lateinit var changeItemCompleteStateUseCase: ChangeShoppingListItemCompleteStateUseCase
    private lateinit var observeShoppingListUseCase: ObserveShoppingListUseCase

    private val contentFlow = MutableStateFlow<Loadable<ShoppingListContent>>(Loadable.Loading)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        setVisibleRangeUseCase = mock()
        changeItemCompleteStateUseCase = mock()
        observeShoppingListUseCase = mock()
        every { observeShoppingListUseCase() } returns contentFlow
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = ShoppingListViewModel(
        setVisibleRangeUseCase = setVisibleRangeUseCase,
        changeItemCompleteStateUseCase = changeItemCompleteStateUseCase,
        observeShoppingListUseCase = observeShoppingListUseCase,
    )

    private fun item(id: String, completed: Boolean) = ShoppingListItem(
        id = id,
        name = "Name $id",
        completed = completed,
        createdByUserId = "user-1",
        createdAt = Instant.fromEpochMilliseconds(0),
        updatedAt = Instant.fromEpochMilliseconds(0),
    )

    private fun content(vararg items: ShoppingListItem) = Loadable.Content(
        ShoppingListContent(
            itemsById = items.associateBy { it.id },
            order = items.map { it.id },
        ),
    )

    @Test
    fun `maps content to ui state`() = runTest {
        contentFlow.value = content(item("i1", completed = false))

        val viewModel = createViewModel()

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state is ShoppingListScreenState.Content)
            assertEquals(emptySet(), state.pendingItemIds)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onToggleItem adds item to pending then removes it after success`() = runTest {
        contentFlow.value = content(item("i1", completed = false))

        val gate = CompletableDeferred<ShoppingListRepository.ChangeCompleteStateResult>()
        everySuspend { changeItemCompleteStateUseCase(any(), any()) } calls { gate.await() }

        val viewModel = createViewModel()

        viewModel.state.test {
            assertEquals(
                emptySet(),
                (awaitItem() as ShoppingListScreenState.Content).pendingItemIds,
            )

            viewModel.onToggleItem(itemId = "i1", completed = true)

            assertEquals(
                setOf("i1"),
                (awaitItem() as ShoppingListScreenState.Content).pendingItemIds,
            )

            gate.complete(ShoppingListRepository.ChangeCompleteStateResult.Success)

            assertEquals(
                emptySet(),
                (awaitItem() as ShoppingListScreenState.Content).pendingItemIds,
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onToggleItem emits ChangeFailed event on non-success result`() = runTest {
        contentFlow.value = content(item("i1", completed = false))
        everySuspend {
            changeItemCompleteStateUseCase(any(), any())
        } returns ShoppingListRepository.ChangeCompleteStateResult.NetworkError

        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onToggleItem(itemId = "i1", completed = true)
            assertEquals(ShoppingListEvent.ChangeFailed, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onToggleItem does not emit event on success`() = runTest {
        contentFlow.value = content(item("i1", completed = false))
        everySuspend {
            changeItemCompleteStateUseCase(any(), any())
        } returns ShoppingListRepository.ChangeCompleteStateResult.Success

        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onToggleItem(itemId = "i1", completed = true)
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onToggleItem ignores a second tap while the first is pending`() = runTest {
        contentFlow.value = content(item("i1", completed = false))

        val gate = CompletableDeferred<ShoppingListRepository.ChangeCompleteStateResult>()
        everySuspend { changeItemCompleteStateUseCase(any(), any()) } calls { gate.await() }

        val viewModel = createViewModel()

        viewModel.onToggleItem(itemId = "i1", completed = true)
        viewModel.onToggleItem(itemId = "i1", completed = true)

        gate.complete(ShoppingListRepository.ChangeCompleteStateResult.Success)

        verifySuspend(exactly(1)) {
            changeItemCompleteStateUseCase("i1", true)
        }
    }

    @Test
    fun `onVisibleRangeChanged delegates to use case`() = runTest {
        every { setVisibleRangeUseCase(any(), any()) } returns Unit

        val viewModel = createViewModel()
        viewModel.onVisibleRangeChanged(firstVisibleIndex = 10, lastVisibleIndex = 20)

        verify(exactly(1)) { setVisibleRangeUseCase(10, 20) }
    }
}