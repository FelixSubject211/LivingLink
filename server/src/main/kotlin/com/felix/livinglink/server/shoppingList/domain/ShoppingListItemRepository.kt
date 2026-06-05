package com.felix.livinglink.server.shoppingList.domain

import com.felix.livinglink.server.core.domain.CrudRepository

interface ShoppingListItemRepository : CrudRepository<ShoppingListItem> {
    suspend fun find(query: ShoppingListItemQuery): List<ShoppingListItem>
}
