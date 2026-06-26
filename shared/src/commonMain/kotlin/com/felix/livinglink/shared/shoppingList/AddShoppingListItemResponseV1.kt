package com.felix.livinglink.shared.shoppingList

import kotlinx.serialization.Serializable

@Serializable
data class AddShoppingListItemResponseV1(
    val item: ShoppingListItemDtoV1
)