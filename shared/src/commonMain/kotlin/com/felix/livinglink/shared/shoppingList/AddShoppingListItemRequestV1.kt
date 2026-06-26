package com.felix.livinglink.shared.shoppingList

import kotlinx.serialization.Serializable

@Serializable
data class AddShoppingListItemRequestV1(
    val groupId: String,
    val name: String,
) {
    companion object {
        const val ROUTE = "/add-shopping-list-item-v1/"
    }
}