package felix.projekt.livinglink.composeApp.shoppingList.application

import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.EventTopic

object ShoppingListTopic : EventTopic {
    override val value: String = "ShoppingList"
}