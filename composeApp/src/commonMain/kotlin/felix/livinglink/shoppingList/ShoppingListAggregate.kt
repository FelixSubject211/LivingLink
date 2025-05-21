package felix.livinglink.shoppingList

data class ShoppingListAggregate(
    val items: LinkedHashMap<String, Item> = linkedMapOf()
) {
    data class Item(
        val id: String,
        val name: String,
        val isCompleted: Boolean
    )

    fun asReversedList() = this.items.values.toList().reversed()
}