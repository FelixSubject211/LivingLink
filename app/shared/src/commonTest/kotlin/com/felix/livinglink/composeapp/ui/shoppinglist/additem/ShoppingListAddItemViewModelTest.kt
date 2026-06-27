package com.felix.livinglink.composeapp.ui.shoppinglist.additem

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.felix.livinglink.composeapp.shoppingList.application.AddShoppingListItemUseCase
import com.felix.livinglink.composeapp.shoppingList.application.ObserveItemSuggestionsUseCase
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListItemSuggestion
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.exactly
import dev.mokkery.verifySuspend
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ShoppingListAddItemViewModelTest {

    private lateinit var addShoppingListItemUseCase: AddShoppingListItemUseCase
    private lateinit var observeItemSuggestionsUseCase: ObserveItemSuggestionsUseCase

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        addShoppingListItemUseCase = mock()
        observeItemSuggestionsUseCase = mock()
        every { observeItemSuggestionsUseCase(any()) } returns flowOf(emptyList())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = ShoppingListAddItemViewModel(
        addShoppingListItemUseCase = addShoppingListItemUseCase,
        observeItemSuggestionsUseCase = observeItemSuggestionsUseCase,
    )

    @Test
    fun `onQueryChanged updates state and enables submit`() = runTest {
        val viewModel = createViewModel()

        viewModel.state.test {
            awaitItem()

            viewModel.onQueryChanged("milk")

            val state = awaitItem()
            assertEquals("milk", state.query)
            assertTrue(state.canSubmit)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `canSubmit is false for blank query`() = runTest {
        val viewModel = createViewModel()

        viewModel.state.test {
            awaitItem()

            viewModel.onQueryChanged("   ")

            assertFalse(awaitItem().canSubmit)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onSubmit calls use case with trimmed query`() = runTest {
        everySuspend { addShoppingListItemUseCase(any()) } returns
            AddShoppingListItemUseCase.Result.Success

        val viewModel = createViewModel()

        viewModel.onQueryChanged("  milk  ")
        viewModel.onSubmit()

        verifySuspend(exactly(1)) { addShoppingListItemUseCase("milk") }
    }

    @Test
    fun `onSubmit clears query and emits Added on success`() = runTest {
        everySuspend { addShoppingListItemUseCase(any()) } returns
            AddShoppingListItemUseCase.Result.Success

        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onQueryChanged("milk")
            viewModel.onSubmit()

            assertEquals(AddItemEvent.Added, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals("", viewModel.state.value.query)
    }

    @Test
    fun `onSubmit emits AddFailed and keeps query on failure`() = runTest {
        everySuspend { addShoppingListItemUseCase(any()) } returns
            AddShoppingListItemUseCase.Result.NetworkError

        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onQueryChanged("milk")
            viewModel.onSubmit()

            assertEquals(AddItemEvent.AddFailed, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.state.test {
            assertEquals("milk", expectMostRecentItem().query)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onSubmit does nothing when query is blank`() = runTest {
        val viewModel = createViewModel()

        viewModel.onQueryChanged("   ")
        viewModel.onSubmit()

        verifySuspend(exactly(0)) { addShoppingListItemUseCase(any()) }
    }

    @Test
    fun `onSubmit sets isAdding while in flight then clears it`() = runTest {
        val gate = CompletableDeferred<AddShoppingListItemUseCase.Result>()
        everySuspend { addShoppingListItemUseCase(any()) } calls { gate.await() }

        val viewModel = createViewModel()

        viewModel.state.test {
            awaitItem()

            viewModel.onQueryChanged("milk")
            assertEquals("milk", awaitItem().query)

            viewModel.onSubmit()
            assertTrue(awaitItem().isAdding)

            gate.complete(AddShoppingListItemUseCase.Result.Success)

            awaitItemMatching { !it.isAdding && it.query == "" }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onSubmit ignores a second submit while the first is in flight`() = runTest {
        val gate = CompletableDeferred<AddShoppingListItemUseCase.Result>()
        everySuspend { addShoppingListItemUseCase(any()) } calls { gate.await() }

        val viewModel = createViewModel()

        viewModel.onQueryChanged("milk")
        viewModel.onSubmit()
        viewModel.onSubmit()

        gate.complete(AddShoppingListItemUseCase.Result.Success)

        verifySuspend(exactly(1)) { addShoppingListItemUseCase("milk") }
    }

    @Test
    fun `onSuggestionSelected adds the suggestion name`() = runTest {
        everySuspend { addShoppingListItemUseCase(any()) } returns
            AddShoppingListItemUseCase.Result.Success

        val viewModel = createViewModel()

        viewModel.onSuggestionSelected(ShoppingListItemSuggestion(name = "Bread", usageCount = 3))

        verifySuspend(exactly(1)) { addShoppingListItemUseCase("Bread") }
    }

    @Test
    fun `onSuggestionSelected ignores tap while adding`() = runTest {
        val gate = CompletableDeferred<AddShoppingListItemUseCase.Result>()
        everySuspend { addShoppingListItemUseCase(any()) } calls { gate.await() }

        val viewModel = createViewModel()

        viewModel.onSuggestionSelected(ShoppingListItemSuggestion(name = "Bread", usageCount = 3))
        viewModel.onSuggestionSelected(ShoppingListItemSuggestion(name = "Bread", usageCount = 3))

        gate.complete(AddShoppingListItemUseCase.Result.Success)

        verifySuspend(exactly(1)) { addShoppingListItemUseCase("Bread") }
    }

    @Test
    fun `suggestions from use case are reflected in state`() = runTest {
        every { observeItemSuggestionsUseCase(any()) } returns flowOf(
            listOf(ShoppingListItemSuggestion(name = "Milk", usageCount = 5)),
        )

        val viewModel = createViewModel()

        viewModel.state.test {
            val state = awaitItemMatching { it.suggestions.isNotEmpty() }
            assertEquals("Milk", state.suggestions.first().name)
            assertTrue(state.showSuggestions)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private suspend fun ReceiveTurbine<ShoppingListAddItemState>.awaitItemMatching(
        predicate: (ShoppingListAddItemState) -> Boolean,
    ): ShoppingListAddItemState {
        while (true) {
            val item = awaitItem()
            if (predicate(item)) return item
        }
    }
}