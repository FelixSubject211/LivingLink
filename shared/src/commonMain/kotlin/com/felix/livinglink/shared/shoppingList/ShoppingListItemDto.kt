package com.felix.livinglink.shared.shoppingList

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ShoppingListItemDto(
    val id: String,
    val name: String,
    val completed: Boolean,
    val createdByUserId: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)