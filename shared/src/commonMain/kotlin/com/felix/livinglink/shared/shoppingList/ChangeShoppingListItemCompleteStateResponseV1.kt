package com.felix.livinglink.shared.shoppingList

import kotlinx.serialization.Serializable

@Serializable
sealed class ChangeShoppingListItemCompleteStateResponseV1 {
    @Serializable
    data class Changed(val item: ShoppingListItemDtoV1) : ChangeShoppingListItemCompleteStateResponseV1()

    @Serializable
    data class AlreadyInState(val item: ShoppingListItemDtoV1) : ChangeShoppingListItemCompleteStateResponseV1()

    @Serializable
    data object NotFound : ChangeShoppingListItemCompleteStateResponseV1()

    @Serializable
    data object Conflict : ChangeShoppingListItemCompleteStateResponseV1()
}