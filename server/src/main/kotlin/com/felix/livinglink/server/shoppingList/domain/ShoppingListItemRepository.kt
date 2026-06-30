package com.felix.livinglink.server.shoppingList.domain

import com.felix.livinglink.server.core.domain.CrudRepository

interface ShoppingListItemRepository : CrudRepository<ShoppingListItem> {
    suspend fun find(query: ShoppingListItemQuery): List<ShoppingListItem>

    suspend fun count(query: ShoppingListItemQuery): Long

    suspend fun findLastPosition(groupId: String): String?

    suspend fun findByIds(ids: Collection<String>): List<ShoppingListItem>

    suspend fun findPositionBelow(
        groupId: String,
        position: String,
        excludingIds: Set<String>,
    ): String?

    suspend fun findPositionAbove(
        groupId: String,
        position: String,
        excludingIds: Set<String>,
    ): String?
}
