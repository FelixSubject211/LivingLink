package com.felix.livinglink.server.shoppingList.infrastructure.mongo

import com.felix.livinglink.server.core.infrastructure.mongo.AbstractMongoRepositoryTest
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItemQuery
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class MongoShoppingListItemRepositoryTest : AbstractMongoRepositoryTest() {
    private lateinit var collection: MongoCollection<MongoShoppingListItemDocument>
    private lateinit var repository: MongoShoppingListItemRepository

    @BeforeTest
    fun setUpRepository() {
        collection = database.getCollection<MongoShoppingListItemDocument>("shopping_list_items")

        runBlocking {
            collection.drop()
        }

        repository = MongoShoppingListItemRepository(collection)
    }

    @Test
    fun `should only return items of the queried group`() =
        runTest {
            val inGroup1 = createDocument(id = "a", name = "Milk", groupId = "group-1", position = "a0")
            val alsoGroup1 = createDocument(id = "b", name = "Bread", groupId = "group-1", position = "a1")
            val inGroup2 = createDocument(id = "c", name = "Eggs", groupId = "group-2", position = "a0")
            collection.insertMany(listOf(inGroup1, alsoGroup1, inGroup2))

            val group1Items =
                repository.find(
                    ShoppingListItemQuery(
                        groupId = "group-1",
                        limit = 10,
                        offset = 0,
                    ),
                )

            assertEquals(listOf("b", "a"), group1Items.map { it.id })
        }

    @Test
    fun `should filter by completed status within the group`() =
        runTest {
            val item1 = createDocument(id = "1", name = "Milk", completed = false, position = "a0")
            val item2 = createDocument(id = "2", name = "Bread", completed = true, position = "a1")
            val item3 = createDocument(id = "3", name = "Eggs", completed = false, position = "a2")
            val otherGroup = createDocument(id = "4", name = "Cheese", completed = false, groupId = "group-2", position = "a0")
            collection.insertMany(listOf(item1, item2, item3, otherGroup))

            val openItems =
                repository.find(
                    ShoppingListItemQuery(groupId = "group-1", completed = false, limit = 10, offset = 0),
                )

            val completedItems =
                repository.find(
                    ShoppingListItemQuery(groupId = "group-1", completed = true, limit = 10, offset = 0),
                )

            val allItems =
                repository.find(
                    ShoppingListItemQuery(groupId = "group-1", completed = null, limit = 10, offset = 0),
                )

            assertEquals(listOf("3", "1"), openItems.map { it.id })
            assertEquals(listOf("2"), completedItems.map { it.id })
            assertEquals(listOf("3", "2", "1"), allItems.map { it.id })
        }

    @Test
    fun `should sort items descending by position then id`() =
        runTest {
            val item1 = createDocument(id = "a", name = "Banana", position = "a1")
            val item2 = createDocument(id = "b", name = "Apple", position = "a0")

            collection.insertMany(listOf(item1, item2))

            val sorted =
                repository.find(
                    ShoppingListItemQuery(groupId = "group-1", limit = 10, offset = 0),
                )
            assertEquals(listOf("a", "b"), sorted.map { it.id })
        }

    @Test
    fun `should break position ties deterministically by id descending`() =
        runTest {
            val item1 = createDocument(id = "b", name = "Banana", position = "a0")
            val item2 = createDocument(id = "a", name = "Apple", position = "a0")

            collection.insertMany(listOf(item1, item2))

            val sorted =
                repository.find(
                    ShoppingListItemQuery(groupId = "group-1", limit = 10, offset = 0),
                )
            assertEquals(listOf("b", "a"), sorted.map { it.id })
        }

    @Test
    fun `should respect limit parameter`() =
        runTest {
            val items = (1..5).map { createDocument(id = it.toString(), name = "Item $it", position = "a$it") }
            collection.insertMany(items)

            val limited =
                repository.find(
                    ShoppingListItemQuery(groupId = "group-1", limit = 2, offset = 0),
                )

            assertEquals(2, limited.size)
            assertEquals(listOf("5", "4"), limited.map { it.id })
        }

    @Test
    fun `should respect offset (skip) parameter`() =
        runTest {
            val items = (1..5).map { createDocument(id = it.toString(), name = "Item $it", position = "a$it") }
            collection.insertMany(items)

            val page =
                repository.find(
                    ShoppingListItemQuery(
                        groupId = "group-1",
                        limit = 2,
                        offset = 2,
                    ),
                )

            assertEquals(listOf("3", "2"), page.map { it.id })
        }

    @Test
    fun `findLastPosition returns the highest position in the group`() =
        runTest {
            collection.insertMany(
                listOf(
                    createDocument(id = "1", name = "Milk", groupId = "group-1", position = "a0"),
                    createDocument(id = "2", name = "Bread", groupId = "group-1", position = "a5"),
                    createDocument(id = "3", name = "Eggs", groupId = "group-1", position = "a2"),
                    createDocument(id = "4", name = "Cheese", groupId = "group-2", position = "a9"),
                ),
            )

            assertEquals("a5", repository.findLastPosition("group-1"))
        }

    @Test
    fun `findLastPosition returns null for an empty group`() =
        runTest {
            assertEquals(null, repository.findLastPosition("group-1"))
        }

    @Test
    fun `count should only count items of the queried group`() =
        runTest {
            collection.insertMany(
                listOf(
                    createDocument(id = "1", name = "Milk", groupId = "group-1", position = "a0"),
                    createDocument(id = "2", name = "Bread", groupId = "group-1", position = "a1"),
                    createDocument(id = "3", name = "Eggs", groupId = "group-2", position = "a0"),
                ),
            )

            val count =
                repository.count(
                    ShoppingListItemQuery(groupId = "group-1", limit = 10, offset = 0),
                )

            assertEquals(2, count)
        }

    @Test
    fun `count should respect the completed filter`() =
        runTest {
            collection.insertMany(
                listOf(
                    createDocument(id = "1", name = "Milk", completed = false, position = "a0"),
                    createDocument(id = "2", name = "Bread", completed = true, position = "a1"),
                    createDocument(id = "3", name = "Eggs", completed = false, position = "a2"),
                    createDocument(id = "4", name = "Cheese", completed = false, groupId = "group-2", position = "a0"),
                ),
            )

            val open =
                repository.count(
                    ShoppingListItemQuery(groupId = "group-1", completed = false, limit = 10, offset = 0),
                )

            val completed =
                repository.count(
                    ShoppingListItemQuery(groupId = "group-1", completed = true, limit = 10, offset = 0),
                )

            val all =
                repository.count(
                    ShoppingListItemQuery(groupId = "group-1", completed = null, limit = 10, offset = 0),
                )

            assertEquals(2, open)
            assertEquals(1, completed)
            assertEquals(3, all)
        }

    @Test
    fun `count should ignore limit and offset`() =
        runTest {
            val items = (1..5).map { createDocument(id = it.toString(), name = "Item $it", position = "a$it") }
            collection.insertMany(items)

            val count =
                repository.count(
                    ShoppingListItemQuery(groupId = "group-1", limit = 2, offset = 3),
                )

            assertEquals(5, count)
        }

    private fun createDocument(
        id: String,
        name: String,
        groupId: String = "group-1",
        completed: Boolean = false,
        position: String = "a0",
        createdAt: Instant = Instant.fromEpochValue(),
        updatedAt: Instant = Instant.fromEpochValue(),
    ) = MongoShoppingListItemDocument(
        id = id,
        groupId = groupId,
        name = name,
        createdByUserId = "user-123",
        position = position,
        completed = completed,
        completionEvents = emptyList(),
        createdAt = createdAt,
        updatedAt = updatedAt,
        version = 0L,
    )

    companion object {
        private fun Instant.Companion.fromEpochValue(): Instant = fromEpochMilliseconds(1716474000000L)
    }
}
