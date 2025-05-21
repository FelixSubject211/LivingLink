package felix.livinglink.shoppingList

import kotlinx.serialization.Serializable

@Serializable
data class ShoppingListAggregate(
    val items: LinkedHashMap<String, Item> = linkedMapOf()
) {
    @Serializable
    data class Item(
        val id: String,
        val name: String,
        val isCompleted: Boolean
    )

    fun asReversedList() = this.items.values.toList().reversed()
}