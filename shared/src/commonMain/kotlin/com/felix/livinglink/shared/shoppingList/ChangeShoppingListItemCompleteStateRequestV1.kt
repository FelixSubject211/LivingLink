package com.felix.livinglink.shared.shoppingList

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ChangeShoppingListItemCompleteStateRequestV1(
    val groupId: String,
    val itemId: String,
    val completed: Boolean,
    val at: Instant,
) {
    companion object {
        const val ROUTE = "/change-shopping-list-item-v1/"
    }
}