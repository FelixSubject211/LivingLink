package com.felix.livinglink.composeapp.ui.shoppinglist

import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListContent

sealed interface ShoppingListScreenState {
    data object Loading : ShoppingListScreenState

    data object Error : ShoppingListScreenState

    data class Content(
        val shoppingList: ShoppingListContent,
        val pendingItemIds: Set<String> = emptySet(),
    ) : ShoppingListScreenState

    companion object {
        val initial: ShoppingListScreenState = Loading
    }
}