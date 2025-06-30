package felix.livinglink.shoppingList

import felix.livinglink.eventSourcing.EventSourcingEvent
import felix.livinglink.eventSourcing.repository.Aggregate
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class ShoppingListItemHistoryAggregate(
    val itemId: String,
    val itemName: String? = null,
    private val events: List<EventSourcingEvent<ShoppingListEvent>> = emptyList()
) : Aggregate<ShoppingListItemHistoryAggregate, ShoppingListEvent> {

    fun history(): List<EventSourcingEvent<ShoppingListEvent>> = events

    override fun applyEvent(
        event: EventSourcingEvent<ShoppingListEvent>
    ): ShoppingListItemHistoryAggregate {
        return if (event.payload.itemId != itemId) {
            this
        } else {
            when (val payload = event.payload) {
                is ShoppingListEvent.ItemAdded -> {
                    copy(
                        itemName = payload.itemName,
                        events = events + event
                    )
                }

                else -> {
                    copy(events = events + event)
                }
            }
        }
    }

    override fun isEmpty(): Boolean = events.isEmpty()

    @OptIn(ExperimentalUuidApi::class)
    override fun anonymizeUser(originalUserId: String): ShoppingListItemHistoryAggregate {
        return copy(
            events = events.map { event ->
                if (event.userId == originalUserId) {
                    event.copy(userId = Uuid.random().toString())
                } else {
                    event
                }
            }
        )
    }

    @OptIn(InternalSerializationApi::class)
    override fun serializer(): KSerializer<out ShoppingListItemHistoryAggregate> {
        return this::class.serializer()
    }

    companion object {
        fun empty(itemId: String) = ShoppingListItemHistoryAggregate(
            itemId = itemId
        )
    }
}