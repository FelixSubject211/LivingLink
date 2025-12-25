package felix.projekt.livinglink.composeApp.ui.shoppingList.viewModel

import felix.projekt.livinglink.composeApp.ui.core.viewmodel.Reducer

class ShoppingListReducer : Reducer<ShoppingListState, ShoppingListResult> {
    override fun invoke(
        state: ShoppingListState,
        result: ShoppingListResult
    ): ShoppingListState {
        return when (result) {
            is ShoppingListResult.Init -> {
                state.copy(pagingModel = result.pagingModel)
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
                state.copy(
                    submittingItemIds = state.submittingItemIds + result.itemId
                )
            }

            is ShoppingListResult.ItemCheckedFinished -> {
                state.copy(
                    submittingItemIds = state.submittingItemIds - result.itemId
                )
            }

            is ShoppingListResult.ItemUncheckedSubmitting -> {
                state.copy(
                    submittingItemIds = state.submittingItemIds + result.itemId
                )
            }

            is ShoppingListResult.ItemUncheckedFinished -> {
                state.copy(
                    submittingItemIds = state.submittingItemIds - result.itemId
                )
            }
        }
    }
}