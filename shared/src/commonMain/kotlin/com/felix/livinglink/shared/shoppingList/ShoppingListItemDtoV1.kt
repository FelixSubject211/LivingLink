package com.felix.livinglink.shared.shoppingList

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ShoppingListItemDtoV1(
    val id: String,
    val name: String,
    val position: String,
    val completed: Boolean,
    val createdByUserId: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)