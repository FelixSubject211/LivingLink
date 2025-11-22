package felix.projekt.livinglink.composeApp.shoppingList.domain

data class ShoppingListState(
    val itemIdToItem: Map<String, Item>
) {
    data class Item(
        val id: String,
        val name: String,
        val isChecked: Boolean
    )
}