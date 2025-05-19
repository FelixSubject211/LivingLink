package felix.livinglink.shoppingList

import felix.livinglink.eventSourcing.EventSourcingEvent

interface ShoppingListReducer {
    operator fun invoke(events: List<EventSourcingEvent>): ShoppingListAggregate
}

class ShoppingListDefaultReducer : ShoppingListReducer {
    override fun invoke(events: List<EventSourcingEvent>): ShoppingListAggregate {
        val payloads = events.map {
            it.payload as ShoppingListEvent
        }

        val grouped = payloads.groupBy { it.itemId }

        val items = grouped.mapNotNull { (itemId, history) ->
            val name = history
                .filterIsInstance<ShoppingListEvent.ItemAdded>()
                .lastOrNull()?.itemName ?: return@mapNotNull null

            val isCompleted = history.lastOrNull {
                it is ShoppingListEvent.ItemCompleted || it is ShoppingListEvent.ItemUncompleted
            }?.let {
                when (it) {
                    is ShoppingListEvent.ItemCompleted -> true
                    is ShoppingListEvent.ItemUncompleted -> false
                    else -> false
                }
            } ?: false

            ShoppingListAggregate.Item(
                id = itemId,
                name = name,
                isCompleted = isCompleted,
                history = history
            )
        }.reversed()

        return ShoppingListAggregate(items)
    }
}