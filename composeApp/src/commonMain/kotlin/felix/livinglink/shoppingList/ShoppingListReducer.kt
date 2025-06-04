package felix.livinglink.shoppingList

import felix.livinglink.eventSourcing.EventSourcingEvent

interface ShoppingListReducer {
    operator fun invoke(
        state: ShoppingListAggregate,
        event: EventSourcingEvent
    ): ShoppingListAggregate
}

class ShoppingListDefaultReducer : ShoppingListReducer {

    override fun invoke(
        state: ShoppingListAggregate,
        event: EventSourcingEvent
    ): ShoppingListAggregate {

        val payload = event.payload as? ShoppingListEvent ?: return state
        val newItems = LinkedHashMap(state.items)

        when (payload) {
            is ShoppingListEvent.ItemAdded -> {
                newItems[payload.itemId] = ShoppingListAggregate.Item(
                    id = payload.itemId,
                    name = payload.itemName,
                    isCompleted = false
                )
            }

            is ShoppingListEvent.ItemCompleted,
            is ShoppingListEvent.ItemUncompleted -> {
                val current = newItems[payload.itemId] ?: return state
                newItems[payload.itemId] = current.copy(
                    isCompleted = payload is ShoppingListEvent.ItemCompleted
                )
            }
        }
        return state.copy(items = newItems)
    }
}