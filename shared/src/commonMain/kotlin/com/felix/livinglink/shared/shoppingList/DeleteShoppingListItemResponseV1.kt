package com.felix.livinglink.shared.shoppingList

import kotlinx.serialization.Serializable

@Serializable
sealed class DeleteShoppingListItemResponseV1 {
    @Serializable
    data object Deleted : DeleteShoppingListItemResponseV1()

    @Serializable
    data object NotFound : DeleteShoppingListItemResponseV1()
}