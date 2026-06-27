package com.felix.livinglink.shared.shoppingList

import kotlinx.serialization.Serializable

@Serializable
data class DeleteShoppingListItemRequestV1(
    val groupId: String,
    val itemId: String,
) {
    companion object {
        const val ROUTE = "/delete-shopping-list-item-v1/"
    }
}