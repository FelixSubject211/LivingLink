package com.felix.livinglink.shared.shoppingList

import kotlinx.serialization.Serializable

@Serializable
data class GetShoppingListItemsPageResponseV1(
    val items: List<ShoppingListItemDtoV1>,
    val totalCount: Int,
)