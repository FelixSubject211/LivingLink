package com.felix.livinglink.composeapp.ui.shoppinglist

import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListContent
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListItem

sealed interface ShoppingListScreenState {
    data object Loading : ShoppingListScreenState

    data object Error : ShoppingListScreenState

    data class Content(
        val shoppingList: ShoppingListContent,
        val pendingItemIds: Set<String> = emptySet(),
        val itemPendingDelete: ShoppingListItem? = null,
    ) : ShoppingListScreenState

    companion object {
        val initial: ShoppingListScreenState = Loading
    }
}