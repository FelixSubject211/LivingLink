package com.felix.livinglink.shared.shoppingList

import kotlinx.serialization.Serializable

@Serializable
enum class ShoppingListItemSortV1 {
    CreatedAtAscending,
    CreatedAtDescending,
    UpdatedAtAscending,
    UpdatedAtDescending,
    NameAscending,
    NameDescending,
}