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

    override fun applyEvent(event: EventSourcingEvent<ShoppingListEvent>): ShoppingListAggregate {
        val newOpenItems = LinkedHashMap(openItems)
        val newCompletedItems = LinkedHashMap(completedItems)

        when (val payload = event.payload) {
            is ShoppingListEvent.ItemAdded -> {
                val item = Item(id = payload.itemId, name = payload.itemName, isCompleted = false)
                newOpenItems[payload.itemId] = item
                newCompletedItems.remove(payload.itemId)
            }

            is ShoppingListEvent.ItemCompleted -> {
                newOpenItems.remove(payload.itemId)?.let {
                    newCompletedItems[payload.itemId] = it.copy(isCompleted = true)
                }
            }

            is ShoppingListEvent.ItemUncompleted -> {
                newCompletedItems.remove(payload.itemId)?.let {
                    newOpenItems[payload.itemId] = it.copy(isCompleted = false)
                }
            }

            is ShoppingListEvent.ItemDeleted -> {
                newOpenItems.remove(payload.itemId)
                newCompletedItems.remove(payload.itemId)
            }
        }

        return copy(openItems = newOpenItems, completedItems = newCompletedItems)
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