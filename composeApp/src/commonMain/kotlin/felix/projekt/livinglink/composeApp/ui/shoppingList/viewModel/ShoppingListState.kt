package felix.projekt.livinglink.composeApp.ui.shoppingList.viewModel

import felix.projekt.livinglink.composeApp.core.domain.PagingModel

data class ShoppingListState(
    val pagingModel: PagingModel<Item>? = null,
    val submittingItemIds: Set<String> = emptySet(),
    val newItemName: String = ""
) {
    data class Item(
        val id: String,
        val name: String?,
        val isChecked: Boolean
    )
}