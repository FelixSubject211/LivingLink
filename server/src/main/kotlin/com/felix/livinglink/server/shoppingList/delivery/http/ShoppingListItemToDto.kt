package com.felix.livinglink.server.shoppingList.delivery.http

import com.felix.livinglink.server.shoppingList.domain.ShoppingListItem
import com.felix.livinglink.shared.shoppingList.ShoppingListItemDtoV1

fun ShoppingListItem.toDtoV1(): ShoppingListItemDtoV1 =
    ShoppingListItemDtoV1(
        id = id,
        name = name,
        position = position,
        completed = isCompleted,
        createdByUserId = createdByUserId,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
