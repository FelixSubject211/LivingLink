package com.felix.livinglink.server.shoppingList.infrastructure.mongo

import com.felix.livinglink.server.shoppingList.domain.completionEvent
import com.felix.livinglink.server.shoppingList.domain.shoppingListItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

class MongoShoppingListItemDocumentTest {
    private val t0 = Instant.fromEpochSeconds(1_700_000_000)

    @Test
    fun `fromDomain maps every field and derives completed from the last event`() {
        val item =
            shoppingListItem(
                id = "id-1",
                groupId = "group-1",
                name = "Bread",
                createdByUserId = "creator",
                createdAt = t0,
                updatedAt = t0 + 2.hours,
                version = 4L,
                completionEvents =
                    listOf(
                        completionEvent(byUserId = "user-a", at = t0, completed = true),
                        completionEvent(byUserId = "user-b", at = t0 + 1.hours, completed = false),
                    ),
            )

        val expected =
            MongoShoppingListItemDocument(
                id = "id-1",
                groupId = "group-1",
                name = "Bread",
                createdByUserId = "creator",
                completed = false,
                completionEvents =
                    listOf(
                        MongoCompletionEventDocument(
                            byUserId = "user-a",
                            completed = true,
                            at = t0,
                        ),
                        MongoCompletionEventDocument(
                            byUserId = "user-b",
                            completed = false,
                            at = t0 + 1.hours,
                        ),
                    ),
                createdAt = t0,
                updatedAt = t0 + 2.hours,
                version = 4L,
            )

        assertEquals(expected, MongoShoppingListItemDocument.fromDomain(item))
    }

    @Test
    fun `roundtrip domain to document to domain preserves the item`() {
        val item =
            shoppingListItem(
                id = "id-1",
                groupId = "group-1",
                name = "Bread",
                createdByUserId = "creator",
                createdAt = t0,
                updatedAt = t0 + 2.hours,
                version = 4L,
                completionEvents =
                    listOf(
                        completionEvent(byUserId = "user-a", at = t0, completed = true),
                        completionEvent(byUserId = "user-b", at = t0 + 1.hours, completed = false),
                        completionEvent(byUserId = "user-c", at = t0 + 2.hours, completed = true),
                    ),
            )

        val roundtripped = MongoShoppingListItemDocument.fromDomain(item).toDomain()

        assertEquals(item, roundtripped)
    }

    @Test
    fun `withVersion returns a copy with the new version`() {
        val document = MongoShoppingListItemDocument.fromDomain(shoppingListItem(version = 2L))

        assertEquals(document.copy(version = 5L), document.withVersion(5L))
    }
}
