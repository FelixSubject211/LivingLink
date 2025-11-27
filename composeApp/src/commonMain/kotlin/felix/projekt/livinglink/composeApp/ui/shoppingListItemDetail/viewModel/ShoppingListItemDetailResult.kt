package felix.projekt.livinglink.composeApp.ui.shoppingListItemDetail.viewModel

import felix.projekt.livinglink.composeApp.ui.shoppingListItemDetail.viewModel.ShoppingListItemDetailState.Action

sealed class ShoppingListItemDetailResult {
    data class Loading(val progress: Float) : ShoppingListItemDetailResult()
    data class DetailLoaded(
        val itemName: String?,
        val actions: List<Action>
    ) : ShoppingListItemDetailResult()
}