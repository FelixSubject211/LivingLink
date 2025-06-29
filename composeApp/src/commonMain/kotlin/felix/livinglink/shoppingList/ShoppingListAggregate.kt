package felix.livinglink.shoppingList

import felix.livinglink.eventSourcing.EventSourcingEvent
import felix.livinglink.eventSourcing.repository.Aggregate
import kotlinx.serialization.Serializable

@Serializable
data class ShoppingListAggregate(
    private val items: LinkedHashMap<String, Item> = linkedMapOf(),
    private val lastEventId: Long? = null
) : Aggregate<ShoppingListAggregate, ShoppingListEvent> {
    @Serializable
    data class Item(
        val id: String,
        val name: String,
        val isCompleted: Boolean
    )

    fun asReversedList(): List<Item> = items.values.reversed()

    override fun applyEvent(event: EventSourcingEvent<ShoppingListEvent>): ShoppingListAggregate {
        val newItems = LinkedHashMap(items)
        when (val payload = event.payload) {
            is ShoppingListEvent.ItemAdded -> {
                newItems[payload.itemId] = Item(
                    id = payload.itemId,
                    name = payload.itemName,
                    isCompleted = false
                )
            }

            is ShoppingListEvent.ItemCompleted,
            is ShoppingListEvent.ItemUncompleted -> {
                newItems[payload.itemId]?.let {
                    newItems[payload.itemId] = it.copy(
                        isCompleted = payload is ShoppingListEvent.ItemCompleted
                    )
                } ?: return copy(lastEventId = event.eventId)
            }
        }
        return copy(items = newItems, lastEventId = event.eventId)
    }

    override fun getLastEventId(): Long? {
        return lastEventId
    }

    override fun isEmpty(): Boolean {
        return items.isEmpty()
    }

    companion object {
        val empty = ShoppingListAggregate()
    }
}