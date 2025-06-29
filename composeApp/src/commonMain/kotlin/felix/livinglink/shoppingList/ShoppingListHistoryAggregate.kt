package felix.livinglink.shoppingList

import felix.livinglink.eventSourcing.EventSourcingEvent
import felix.livinglink.eventSourcing.repository.Aggregate
import kotlinx.serialization.Serializable

@Serializable
data class ShoppingListItemHistoryAggregate(
    val itemId: String,
    val itemName: String? = null,
    private val events: List<EventSourcingEvent<ShoppingListEvent>> = emptyList(),
    private val lastEventId: Long? = null
) : Aggregate<ShoppingListItemHistoryAggregate, ShoppingListEvent> {

    fun history(): List<EventSourcingEvent<ShoppingListEvent>> = events

    override fun applyEvent(
        event: EventSourcingEvent<ShoppingListEvent>
    ): ShoppingListItemHistoryAggregate {
        return if (event.payload.itemId != itemId) {
            copy(
                lastEventId = event.eventId
            )
        } else {
            when (val payload = event.payload) {
                is ShoppingListEvent.ItemAdded -> {
                    copy(
                        itemName = payload.itemName,
                        events = events + event,
                        lastEventId = event.eventId
                    )
                }

                else -> {
                    copy(
                        events = events + event,
                        lastEventId = event.eventId
                    )
                }
            }
        }
    }

    override fun getLastEventId(): Long? = lastEventId

    override fun isEmpty(): Boolean = events.isEmpty()

    companion object {
        fun empty(itemId: String) = ShoppingListItemHistoryAggregate(
            itemId = itemId
        )
    }
}