package com.felix.livinglink.composeapp.shoppingList.domain

data class ShoppingListContent(
    val itemsById: Map<String, ShoppingListItem>,
    val order: List<String?>,
) {
    val totalCount: Int get() = order.size

    fun itemAt(index: Int): ShoppingListItem? =
        order.getOrNull(index)?.let(itemsById::get)

    fun isLoadedAt(index: Int): Boolean = order.getOrNull(index) != null
}