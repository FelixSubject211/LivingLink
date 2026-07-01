package com.felix.livinglink.server.shoppingList.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

class ShoppingListItemTest {
    private val t0 = Instant.fromEpochSeconds(1_700_000_000)

    @Test
    fun `isCompleted follows the last completion event`() {
        val empty = shoppingListItem(completionEvents = emptyList())
        val completed =
            shoppingListItem(
                completionEvents =
                    listOf(
                        completionEvent(at = t0, completed = true),
                    ),
            )
        val reopened =
            shoppingListItem(
                completionEvents =
                    listOf(
                        completionEvent(at = t0, completed = true),
                        completionEvent(at = t0 + 1.hours, completed = false),
                    ),
            )

        assertFalse(empty.isCompleted)
        assertTrue(completed.isCompleted)
        assertFalse(reopened.isCompleted)
    }

    @Test
    fun `referencedUserIds is the creator plus every event author, deduplicated`() {
        val item =
            shoppingListItem(
                createdByUserId = "creator",
                completionEvents =
                    listOf(
                        completionEvent(byUserId = "creator", at = t0),
                        completionEvent(byUserId = "user-a", at = t0 + 1.hours),
                        completionEvent(byUserId = "user-a", at = t0 + 2.hours),
                    ),
            )

        assertEquals(setOf("creator", "user-a"), item.referencedUserIds)
    }

    @Test
    fun `complete appends a completion event, updates updatedAt, keeps everything else`() {
        val original =
            shoppingListItem(
                id = "id-1",
                name = "Milk",
                createdByUserId = "creator",
                createdAt = t0,
                updatedAt = t0,
                version = 7L,
                completionEvents =
                    listOf(
                        completionEvent(byUserId = "user-a", at = t0, completed = true),
                    ),
            )

        val completed = original.complete(byUserId = "user-b", at = t0 + 1.hours)

        val expected =
            original.copy(
                updatedAt = t0 + 1.hours,
                completionEvents =
                    original.completionEvents +
                        ShoppingListItem.CompletionEvent(
                            byUserId = "user-b",
                            completed = true,
                            at = t0 + 1.hours,
                        ),
            )

        assertEquals(expected, completed)
    }

    @Test
    fun `unComplete appends an uncompletion event, updates updatedAt, keeps everything else`() {
        val original =
            shoppingListItem(
                id = "id-1",
                name = "Milk",
                createdByUserId = "creator",
                createdAt = t0,
                updatedAt = t0,
                version = 7L,
                completionEvents =
                    listOf(
                        completionEvent(byUserId = "user-a", at = t0, completed = true),
                    ),
            )

        val unCompleted = original.unComplete(byUserId = "user-b", at = t0 + 1.hours)

        val expected =
            original.copy(
                updatedAt = t0 + 1.hours,
                completionEvents =
                    original.completionEvents +
                        ShoppingListItem.CompletionEvent(
                            byUserId = "user-b",
                            completed = false,
                            at = t0 + 1.hours,
                        ),
            )

        assertEquals(expected, unCompleted)
    }

    @Test
    fun `moveTo updates position and updatedAt, keeps everything else`() {
        val original =
            shoppingListItem(
                id = "id-1",
                name = "Milk",
                createdByUserId = "creator",
                position = "a0",
                createdAt = t0,
                updatedAt = t0,
                version = 7L,
                completionEvents =
                    listOf(
                        completionEvent(byUserId = "user-a", at = t0, completed = true),
                    ),
            )

        val moved = original.moveTo(position = "a5", at = t0 + 1.hours)

        val expected =
            original.copy(
                position = "a5",
                updatedAt = t0 + 1.hours,
            )

        assertEquals(expected, moved)
    }
}
