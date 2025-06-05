package felix.livinglink.shoppingList

import felix.livinglink.eventSourcing.EventSourcingEvent
import felix.livinglink.eventSourcing.repository.Aggregate
import kotlinx.serialization.Serializable

@Serializable
data class ShoppingListAggregate(
    val items: LinkedHashMap<String, Item> = linkedMapOf(),
    val lastEventId: Long = 0L
) : Aggregate<ShoppingListAggregate> {
    @Serializable
    data class Item(
        val id: String,
        val name: String,
        val isCompleted: Boolean
    )

    fun asReversedList(): List<Item> = items.values.reversed()

    override fun applyEvent(event: EventSourcingEvent): ShoppingListAggregate {
        if (event.eventId <= lastEventId) {
            throw IllegalStateException("Stale eventId=${event.eventId}, last=${lastEventId}")
        }
        val payload = event.payload as? ShoppingListEvent ?: return this
        val newItems = LinkedHashMap(items)
        when (payload) {
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
                } ?: return this
            }
        }
        return copy(items = newItems, lastEventId = event.eventId)
    }

    companion object {
        val empty = ShoppingListAggregate()
    }
}