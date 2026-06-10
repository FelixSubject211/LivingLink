package com.felix.livinglink.shared.shoppingList

import kotlinx.serialization.Serializable

@Serializable
data class ListShoppingListItemsResponseV1(
    val items: List<ShoppingListItemDto>,
    val nextCursor: String?,
)