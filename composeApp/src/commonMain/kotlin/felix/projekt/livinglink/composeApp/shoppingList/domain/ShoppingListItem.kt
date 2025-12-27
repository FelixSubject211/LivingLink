package felix.projekt.livinglink.composeApp.shoppingList.domain

import kotlinx.serialization.Serializable

@Serializable
data class ShoppingListItem(
    val id: String,
    val name: String?,
    val isChecked: Boolean
)