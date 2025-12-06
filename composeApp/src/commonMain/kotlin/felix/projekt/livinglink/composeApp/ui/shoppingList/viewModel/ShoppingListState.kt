package felix.projekt.livinglink.composeApp.ui.shoppingList.viewModel

data class ShoppingListState(
    val isLoading: Boolean = false,
    val loadingProgress: Float = 0f,
    val items: List<Item> = emptyList(),
    val submittingItemIds: Set<String> = emptySet(),
    val newItemName: String = ""
) {
    data class Item(
        val id: String,
        val name: String,
        val isChecked: Boolean
    )
}