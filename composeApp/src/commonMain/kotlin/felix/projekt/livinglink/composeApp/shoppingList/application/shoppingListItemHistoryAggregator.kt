package felix.projekt.livinglink.composeApp.shoppingList.application

import felix.projekt.livinglink.composeApp.core.domain.getOrNull
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.Aggregator
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.EventSourcingEvent
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.TopicSubscription
import felix.projekt.livinglink.composeApp.shoppingList.domain.ShoppingListEvent
import felix.projekt.livinglink.composeApp.shoppingList.domain.ShoppingListItemHistoryState
import felix.projekt.livinglink.shared.json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
fun shoppingListItemHistoryAggregator(
    groupId: String,
    itemId: String
): Aggregator<ShoppingListTopic, ShoppingListItemHistoryState> {
    return object : Aggregator<ShoppingListTopic, ShoppingListItemHistoryState> {
        override val id = "shoppingListItemHistoryAggregator-$itemId"

        override val subscription = TopicSubscription(
            groupId = groupId,
            topic = ShoppingListTopic
        )

        override val initialState = ShoppingListItemHistoryState(
            itemId = itemId,
            itemName = null,
            actions = emptyList()
        )

        override fun apply(
            currentState: ShoppingListItemHistoryState,
            events: List<EventSourcingEvent>
        ): ShoppingListItemHistoryState {
            val actions = currentState.actions.toMutableList()
            var itemName = currentState.itemName

            val relevantEvents = events.filter { event ->
                event.payload.jsonObject["id"]?.jsonPrimitive?.content == itemId
            }

            for (event in relevantEvents) {
                val shoppingEvent = event.decode<ShoppingListEvent>()
                val actionType = when (shoppingEvent) {
                    is ShoppingListEvent.ItemCreated -> {
                        itemName = shoppingEvent.name.getOrNull()
                        ShoppingListItemHistoryState.ShoppingListItemHistoryActionType.Created
                    }

                    is ShoppingListEvent.ItemChecked -> {
                        ShoppingListItemHistoryState.ShoppingListItemHistoryActionType.Checked
                    }

                    is ShoppingListEvent.ItemUnchecked -> {
                        ShoppingListItemHistoryState.ShoppingListItemHistoryActionType.Unchecked
                    }

                    is ShoppingListEvent.ItemDeleted -> {
                        ShoppingListItemHistoryState.ShoppingListItemHistoryActionType.Deleted
                    }
                }

                actions.add(
                    ShoppingListItemHistoryState.Action(
                        eventId = event.eventId,
                        userId = event.createdBy,
                        actionType = actionType,
                        createdAt = Instant.fromEpochMilliseconds(event.createdAtEpochMillis)
                    )
                )
            }

            return currentState.copy(
                itemName = itemName,
                actions = actions
            )
        }

        inline fun <reified T> EventSourcingEvent.decode(): T {
            return json.decodeFromJsonElement(payload)
        }
    }
}
