package felix.livinglink.shoppingList

import felix.livinglink.eventSourcing.EventSourcingEvent
import felix.livinglink.eventSourcing.repository.Aggregate
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

@Serializable
data class ShoppingListAggregate(
    private val openItems: LinkedHashMap<String, Item> = linkedMapOf(),
    private val completedItems: LinkedHashMap<String, Item> = linkedMapOf()
) : Aggregate<ShoppingListAggregate, ShoppingListEvent> {

    @Serializable
    data class Item(
        val id: String,
        val name: String,
        val isCompleted: Boolean
    )

    fun openItemsReversed(): List<Item> = openItems.values.reversed()
    fun completedItemsReversed(): List<Item> = completedItems.values.reversed()

    override fun applyEvents(events: List<EventSourcingEvent<ShoppingListEvent>>): ShoppingListAggregate {
        val openItems = LinkedHashMap(this.openItems)
        val completedItems = LinkedHashMap(this.completedItems)

        for (event in events) {
            when (val payload = event.payload) {
                is ShoppingListEvent.ItemAdded -> {
                    val item = Item(payload.itemId, payload.itemName, false)
                    openItems[payload.itemId] = item
                    completedItems.remove(payload.itemId)
                }

                is ShoppingListEvent.ItemCompleted -> {
                    openItems.remove(payload.itemId)?.let {
                        completedItems[payload.itemId] = it.copy(isCompleted = true)
                    }
                }

                is ShoppingListEvent.ItemUncompleted -> {
                    completedItems.remove(payload.itemId)?.let {
                        openItems[payload.itemId] = it.copy(isCompleted = false)
                    }
                }

                is ShoppingListEvent.ItemDeleted -> {
                    openItems.remove(payload.itemId)
                    completedItems.remove(payload.itemId)
                }
            }
        }

        return copy(openItems = openItems, completedItems = completedItems)
    }

    override fun isEmpty(): Boolean = openItems.isEmpty() && completedItems.isEmpty()

    override fun anonymizeUser(originalUserId: String): ShoppingListAggregate = this

    @OptIn(InternalSerializationApi::class)
    override fun serializer(): KSerializer<out ShoppingListAggregate> {
        return this::class.serializer()
    }

    companion object {
        val empty = ShoppingListAggregate()
    }
}