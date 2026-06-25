package com.felix.livinglink.composeapp.shoppingList.domain

import kotlinx.coroutines.flow.Flow

interface ShoppingListLocalDataSource {

    fun observe(groupId: String): Flow<ShoppingListContent?>

    suspend fun putRange(
        groupId: String,
        fromIndex: Int,
        items: List<ShoppingListItem>,
        totalCount: Int,
    )

    suspend fun updateItem(
        groupId: String,
        itemId: String,
        transform: (ShoppingListItem) -> ShoppingListItem,
    )

    suspend fun removeItem(
        groupId: String,
        itemId: String,
    )
}