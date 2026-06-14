package com.felix.livinglink.shared.shoppingList

import kotlinx.serialization.Serializable

@Serializable
data class ChangeShoppingListItemCompleteStateRequestV1(
    val groupId: String,
    val itemId: String,
    val completed: Boolean,
) {
    companion object {
        const val ROUTE = "/shopping-list-item/complete-state/v1"
    }
}