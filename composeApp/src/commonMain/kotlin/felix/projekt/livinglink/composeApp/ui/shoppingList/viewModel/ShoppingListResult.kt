package felix.projekt.livinglink.composeApp.ui.shoppingList.viewModel

import felix.projekt.livinglink.composeApp.core.domain.PagingModel
import felix.projekt.livinglink.composeApp.ui.shoppingList.viewModel.ShoppingListState.Item

sealed class ShoppingListResult {
    data class Init(val pagingModel: PagingModel<Item>) : ShoppingListResult()
    data class NewItemNameUpdated(val name: String) : ShoppingListResult()
    data object AddItemSubmitting : ShoppingListResult()
    data object AddItemFinished : ShoppingListResult()
    data class ItemCheckedSubmitting(val itemId: String) : ShoppingListResult()
    data class ItemCheckedFinished(val itemId: String) : ShoppingListResult()
    data class ItemUncheckedSubmitting(val itemId: String) : ShoppingListResult()
    data class ItemUncheckedFinished(val itemId: String) : ShoppingListResult()
}