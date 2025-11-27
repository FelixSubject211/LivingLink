package felix.projekt.livinglink.composeApp.ui.shoppingListItemDetail.viewModel

import felix.projekt.livinglink.composeApp.ui.core.viewmodel.Reducer

class ShoppingListItemDetailReducer : Reducer<ShoppingListItemDetailState, ShoppingListItemDetailResult> {
    override fun invoke(
        state: ShoppingListItemDetailState,
        result: ShoppingListItemDetailResult
    ): ShoppingListItemDetailState = when (result) {
        is ShoppingListItemDetailResult.Loading -> {
            state.copy(
                isLoading = true,
                loadingProgress = result.progress
            )
        }

        is ShoppingListItemDetailResult.DetailLoaded -> {
            state.copy(
                itemName = result.itemName,
                isLoading = false,
                actions = result.actions
            )
        }

        is ShoppingListItemDetailResult.ShowDeleteConfirmation -> {
            state.copy(
                showDeleteConfirmationDialog = true
            )
        }

        is ShoppingListItemDetailResult.HideDeleteConfirmation -> {
            state.copy(
                showDeleteConfirmationDialog = false,
                isDeleting = false
            )
        }

        is ShoppingListItemDetailResult.Deleting -> {
            state.copy(
                isDeleting = true
            )
        }

        is ShoppingListItemDetailResult.DeleteFinished -> {
            state.copy(
                isDeleting = false,
                showDeleteConfirmationDialog = false
            )
        }
    }
}