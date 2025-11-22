package felix.projekt.livinglink.composeApp.shoppingList.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface ShoppingListEvent {

    @Serializable
    @SerialName("ItemCreated")
    data class ItemCreated(
        val id: String,
        val name: String
    ) : ShoppingListEvent

    @Serializable
    @SerialName("ItemChecked")
    data class ItemChecked(
        val id: String
    ) : ShoppingListEvent
}