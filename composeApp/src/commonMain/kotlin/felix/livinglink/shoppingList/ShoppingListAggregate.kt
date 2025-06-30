package felix.livinglink.shoppingList

import felix.livinglink.eventSourcing.EventSourcingEvent
import felix.livinglink.eventSourcing.repository.Aggregate
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

@Serializable
data class ShoppingListAggregate(
    private val items: LinkedHashMap<String, Item> = linkedMapOf()
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
                } ?: return this
            }
        }
        return copy(items = newItems)
    }

    override fun isEmpty(): Boolean {
        return items.isEmpty()
    }

    override fun anonymizeUser(originalUserId: String): ShoppingListAggregate {
        return this
    }

    @OptIn(InternalSerializationApi::class)
    override fun serializer(): KSerializer<out ShoppingListAggregate> {
        return this::class.serializer()
    }

    companion object {
        val empty = ShoppingListAggregate()
    }
}