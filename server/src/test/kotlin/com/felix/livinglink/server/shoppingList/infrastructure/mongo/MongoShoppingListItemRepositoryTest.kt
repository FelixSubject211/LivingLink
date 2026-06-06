package com.felix.livinglink.server.shoppingList.infrastructure.mongo

import com.felix.livinglink.server.core.infrastructure.mongo.AbstractMongoRepositoryTest
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItemQuery
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItemSort
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds
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
            val inGroup1 = createDocument(id = "1", name = "Milk", groupId = "group-1")
            val alsoGroup1 = createDocument(id = "2", name = "Bread", groupId = "group-1")
            val inGroup2 = createDocument(id = "3", name = "Eggs", groupId = "group-2")
            collection.insertMany(listOf(inGroup1, alsoGroup1, inGroup2))

            val group1Items =
                repository.find(
                    ShoppingListItemQuery(
                        groupId = "group-1",
                        limit = 10,
                        sort = ShoppingListItemSort.NameAscending,
                    ),
                )

            assertEquals(listOf("2", "1"), group1Items.map { it.id })
        }

    @Test
    fun `should filter by completed status within the group`() =
        runTest {
            val item1 = createDocument(id = "1", name = "Milk", completed = false)
            val item2 = createDocument(id = "2", name = "Bread", completed = true)
            val item3 = createDocument(id = "3", name = "Eggs", completed = false)
            val otherGroup = createDocument(id = "4", name = "Cheese", completed = false, groupId = "group-2")
            collection.insertMany(listOf(item1, item2, item3, otherGroup))

            val openItems = repository.find(ShoppingListItemQuery(groupId = "group-1", completed = false, limit = 10))
            val completedItems = repository.find(ShoppingListItemQuery(groupId = "group-1", completed = true, limit = 10))
            val allItems = repository.find(ShoppingListItemQuery(groupId = "group-1", completed = null, limit = 10))

            assertEquals(listOf("1", "3"), openItems.map { it.id })
            assertEquals(listOf("2"), completedItems.map { it.id })
            assertEquals(listOf("1", "2", "3"), allItems.map { it.id })
        }

    @Test
    fun `should sort items correctly by various criteria`() =
        runTest {
            val now = Instant.fromEpochMilliseconds(1716474000000L)
            val item1 = createDocument(id = "1", name = "Banana", createdAt = now.minus(10.seconds), updatedAt = now)
            val item2 = createDocument(id = "2", name = "Apple", createdAt = now, updatedAt = now.minus(10.seconds))
            collection.insertMany(listOf(item1, item2))

            val nameAsc = repository.find(ShoppingListItemQuery(groupId = "group-1", sort = ShoppingListItemSort.NameAscending, limit = 10))
            assertEquals(listOf("2", "1"), nameAsc.map { it.id })

            val nameDesc = repository.find(ShoppingListItemQuery(groupId = "group-1", sort = ShoppingListItemSort.NameDescending, limit = 10))
            assertEquals(listOf("1", "2"), nameDesc.map { it.id })

            val createdAsc = repository.find(ShoppingListItemQuery(groupId = "group-1", sort = ShoppingListItemSort.CreatedAtAscending, limit = 10))
            assertEquals(listOf("1", "2"), createdAsc.map { it.id })

            val createdDesc = repository.find(ShoppingListItemQuery(groupId = "group-1", sort = ShoppingListItemSort.CreatedAtDescending, limit = 10))
            assertEquals(listOf("2", "1"), createdDesc.map { it.id })

            val updatedAsc = repository.find(ShoppingListItemQuery(groupId = "group-1", sort = ShoppingListItemSort.UpdatedAtAscending, limit = 10))
            assertEquals(listOf("2", "1"), updatedAsc.map { it.id })

            val updatedDesc = repository.find(ShoppingListItemQuery(groupId = "group-1", sort = ShoppingListItemSort.UpdatedAtDescending, limit = 10))
            assertEquals(listOf("1", "2"), updatedDesc.map { it.id })
        }

    @Test
    fun `should respect limit parameter`() =
        runTest {
            val items = (1..5).map { createDocument(id = it.toString(), name = "Item $it") }
            collection.insertMany(items)

            val limited = repository.find(ShoppingListItemQuery(groupId = "group-1", limit = 2, sort = ShoppingListItemSort.NameAscending))

            assertEquals(2, limited.size)
            assertEquals(listOf("1", "2"), limited.map { it.id })
        }

    private fun createDocument(
        id: String,
        name: String,
        groupId: String = "group-1",
        completed: Boolean = false,
        createdAt: Instant = Instant.fromEpochValue(),
        updatedAt: Instant = Instant.fromEpochValue(),
    ) = MongoShoppingListItemDocument(
        id = id,
        groupId = groupId,
        name = name,
        createdByUserId = "user-123",
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
