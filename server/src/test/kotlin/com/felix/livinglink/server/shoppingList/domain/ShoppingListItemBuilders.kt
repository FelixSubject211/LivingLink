package com.felix.livinglink.server.shoppingList.domain

import kotlin.time.Instant

private val defaultInstant = Instant.fromEpochSeconds(0)

fun shoppingListItem(
    id: String = "id-1",
    groupId: String = "group-1",
    name: String = "item-$id",
    createdByUserId: String = "creator",
    position: String = "a0",
    completionEvents: List<ShoppingListItem.CompletionEvent> = emptyList(),
    createdAt: Instant = defaultInstant,
    updatedAt: Instant = createdAt,
    version: Long = 0,
): ShoppingListItem =
    ShoppingListItem(
        id = id,
        groupId = groupId,
        name = name,
        createdByUserId = createdByUserId,
        position = position,
        completionEvents = completionEvents,
        createdAt = createdAt,
        updatedAt = updatedAt,
        version = version,
    )

fun completionEvent(
    byUserId: String = "creator",
    completed: Boolean = true,
    at: Instant = defaultInstant,
): ShoppingListItem.CompletionEvent =
    ShoppingListItem.CompletionEvent(
        byUserId = byUserId,
        completed = completed,
        at = at,
    )
