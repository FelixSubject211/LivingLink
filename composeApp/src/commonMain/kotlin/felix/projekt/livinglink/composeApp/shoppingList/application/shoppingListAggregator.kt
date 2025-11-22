package felix.projekt.livinglink.composeApp.shoppingList.application

import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.Aggregator
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.EventSourcingEvent
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.TopicSubscription
import felix.projekt.livinglink.composeApp.shoppingList.domain.ShoppingListEvent
import felix.projekt.livinglink.composeApp.shoppingList.domain.ShoppingListState
import felix.projekt.livinglink.shared.json
import kotlinx.serialization.json.decodeFromJsonElement

fun shoppingListAggregator(groupId: String): Aggregator<ShoppingListTopic, ShoppingListState> {
    return object : Aggregator<ShoppingListTopic, ShoppingListState> {
        override val id = "shoppingListAggregator"

        override val subscription = TopicSubscription(
            groupId = groupId,
            topic = ShoppingListTopic
        )

        override val initialState = ShoppingListState(emptyMap())

        override fun apply(
            currentState: ShoppingListState,
            events: List<EventSourcingEvent>
        ): ShoppingListState {

            val map = currentState.itemIdToItem.toMutableMap()

            for (event in events) {
                val shoppingEvent = event.decode<ShoppingListEvent>()

                when (shoppingEvent) {
                    is ShoppingListEvent.ItemCreated -> {
                        map[shoppingEvent.id] = ShoppingListState.Item(
                            id = shoppingEvent.id,
                            name = shoppingEvent.name,
                            isChecked = false
                        )
                    }

                    is ShoppingListEvent.ItemChecked -> {
                        val existing = map[shoppingEvent.id] ?: continue
                        map[shoppingEvent.id] = existing.copy(isChecked = true)
                    }
                }
            }

            return currentState.copy(
                itemIdToItem = map
            )
        }

        inline fun <reified T> EventSourcingEvent.decode(): T {
            return json.decodeFromJsonElement(payload)
        }
    }
}