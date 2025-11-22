package felix.projekt.livinglink.composeApp.ui.shoppingList.viewModel

import felix.projekt.livinglink.composeApp.ui.core.viewmodel.Reducer

class ShoppingListReducer : Reducer<ShoppingListState, ShoppingListResult> {
    override fun invoke(
        state: ShoppingListState,
        result: ShoppingListResult
    ): ShoppingListState = when (result) {
        is ShoppingListResult.ShoppingListLoading -> {
            state.copy(isLoading = true, loadingProgress = result.progress)
        }

        is ShoppingListResult.ShoppingListChanged -> {
            state.copy(isLoading = false, loadingProgress = 0f, items = result.items.reversed())
        }

        is ShoppingListResult.NewItemNameUpdated -> {
            state.copy(newItemName = result.name)
        }

        is ShoppingListResult.AddItemSubmitting -> {
            state
        }

        is ShoppingListResult.AddItemFinished -> {
            state.copy(newItemName = "")
        }

        is ShoppingListResult.ItemCheckedSubmitting -> {
            state
        }

        is ShoppingListResult.ItemCheckedFinished -> {
            state
        }
    }
}