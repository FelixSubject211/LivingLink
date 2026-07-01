package com.felix.livinglink.shared.shoppingList

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class AddShoppingListItemRequestV1(
    val groupId: String,
    val id: String,
    val name: String,
    val position: String,
    val createdAt: Instant,
) {
    companion object {
        const val ROUTE = "/add-shopping-list-item-v1/"
    }
}