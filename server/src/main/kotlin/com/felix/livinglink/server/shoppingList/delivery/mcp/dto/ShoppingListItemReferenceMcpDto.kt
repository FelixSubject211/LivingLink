package com.felix.livinglink.server.shoppingList.delivery.mcp.dto

import com.felix.livinglink.server.shoppingList.domain.ShoppingListItem
import kotlinx.serialization.Serializable

@Serializable
data class ShoppingListItemReferenceMcpDto(
    val id: String,
    val name: String,
)

fun ShoppingListItem.toMcpReferenceDto(): ShoppingListItemReferenceMcpDto =
    ShoppingListItemReferenceMcpDto(
        id = id,
        name = name,
    )
