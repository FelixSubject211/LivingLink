package felix.livinglink.shoppingList

import felix.livinglink.eventSourcing.EventSourcingEvent
import kotlinx.serialization.Serializable

@Serializable
sealed class ShoppingListEvent : EventSourcingEvent.Payload {
    abstract val itemId: String

    @Serializable
    data class ItemAdded(
        override val itemId: String,
        val itemName: String
    ) : ShoppingListEvent()

    @Serializable
    data class ItemCompleted(
        override val itemId: String
    ) : ShoppingListEvent()

    @Serializable
    data class ItemUncompleted(
        override val itemId: String
    ) : ShoppingListEvent()
}