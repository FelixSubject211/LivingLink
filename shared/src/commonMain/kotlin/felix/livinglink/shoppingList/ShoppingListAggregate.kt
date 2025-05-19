package felix.livinglink.shoppingList

data class ShoppingListAggregate(
    val items: List<Item>
) {
    data class Item(
        val id: String,
        val name: String,
        val isCompleted: Boolean,
        val history: List<ShoppingListEvent>
    )
}