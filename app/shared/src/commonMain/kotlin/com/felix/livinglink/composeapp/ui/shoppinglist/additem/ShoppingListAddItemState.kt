package com.felix.livinglink.composeapp.ui.shoppinglist.additem

import com.felix.livinglink.composeapp.shoppingList.domain.ItemSuggestion

data class ShoppingListAddItemState(
    val query: String,
    val suggestions: List<ItemSuggestion>,
    val isAdding: Boolean,
) {
    val canSubmit: Boolean
        get() = query.isNotBlank() && !isAdding

    val showSuggestions: Boolean
        get() = suggestions.isNotEmpty()

    companion object {
        val initial =
            ShoppingListAddItemState(
                query = "",
                suggestions = emptyList(),
                isAdding = false,
            )
    }
}

sealed interface AddItemEvent {
    data object Added : AddItemEvent
    data object AddFailed : AddItemEvent
}