package com.felix.livinglink.composeapp.shoppingList.data

sealed interface ShoppingListPendingMutation {
    val groupId: String
    val itemId: String

    data class CompleteChange(
        override val groupId: String,
        override val itemId: String,
        val completed: Boolean,
    ) : ShoppingListPendingMutation

    data class Delete(
        override val groupId: String,
        override val itemId: String,
    ) : ShoppingListPendingMutation
}