package com.felix.livinglink.composeapp.shoppingList.domain

data class ShoppingListPage(
    val items: List<ShoppingListItem>,
    val nextCursor: String?,
    val totalCount: Int,
)