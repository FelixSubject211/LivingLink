package felix.projekt.livinglink.composeApp.shoppingList.application

import felix.projekt.livinglink.composeApp.core.domain.getOrNull
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.EventSourcingEvent
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.Projector
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.TopicSubscription
import felix.projekt.livinglink.composeApp.shoppingList.domain.ShoppingListEvent
import felix.projekt.livinglink.composeApp.shoppingList.domain.ShoppingListItem
import felix.projekt.livinglink.shared.json
import kotlinx.serialization.json.decodeFromJsonElement

fun shoppingListProjector(groupId: String): Projector<ShoppingListItem, ShoppingListTopic> {
    return object : Projector<ShoppingListItem, ShoppingListTopic> {
        override val id: String = "shoppingListProjector-$groupId"

        override val subscription = TopicSubscription(
            groupId = groupId,
            topic = ShoppingListTopic
        )

        override val stateSerializer = ShoppingListItem.serializer()

        override fun apply(event: EventSourcingEvent): Projector.ApplyResult<ShoppingListItem> {
            return when (val shoppingEvent = event.decode<ShoppingListEvent>()) {
                is ShoppingListEvent.ItemCreated -> {
                    Projector.ApplyResult.Add(
                        id = shoppingEvent.id,
                        state = ShoppingListItem(
                            id = shoppingEvent.id,
                            name = shoppingEvent.name.getOrNull(),
                            isChecked = false
                        )
                    )
                }

                is ShoppingListEvent.ItemChecked -> {
                    Projector.ApplyResult.Update(
                        id = shoppingEvent.id,
                        update = {
                            it.copy(
                                isChecked = true
                            )
                        },
                    )
                }

                is ShoppingListEvent.ItemUnchecked -> {
                    Projector.ApplyResult.Update(
                        id = shoppingEvent.id,
                        update = {
                            it.copy(
                                isChecked = false
                            )
                        },
                    )
                }

                is ShoppingListEvent.ItemDeleted -> {
                    Projector.ApplyResult.Delete(shoppingEvent.id)
                }
            }
        }

        inline fun <reified T> EventSourcingEvent.decode(): T {
            return json.decodeFromJsonElement(payload)
        }
    }
}

