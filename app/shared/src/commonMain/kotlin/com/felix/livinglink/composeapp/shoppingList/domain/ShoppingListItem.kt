package com.felix.livinglink.composeapp.shoppingList.domain

import kotlin.time.Instant

data class ShoppingListItem(
    val id: String,
    val name: String,
    val completed: Boolean,
    val createdByUserId: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)