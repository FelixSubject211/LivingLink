package com.felix.livinglink.server.group.infrastructure.mongo

import com.felix.livinglink.server.core.infrastructure.mongo.AbstractMongoRepositoryTest
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MongoActiveMcpGroupRepositoryTest : AbstractMongoRepositoryTest() {
    private lateinit var collection: MongoCollection<MongoActiveMcpGroupDocument>
    private lateinit var repository: MongoActiveMcpGroupRepository

    @BeforeTest
    fun setUpRepository() {
        collection = database.getCollection<MongoActiveMcpGroupDocument>("active_mcp_groups")

        runBlocking {
            collection.drop()
        }

        repository = MongoActiveMcpGroupRepository(collection)
    }

    @Test
    fun `getActiveMcpGroupId returns null when nothing is stored for the user`() =
        runTest {
            val result = repository.getActiveMcpGroupId("user-1")

            assertNull(result)
        }

    @Test
    fun `setActiveMcpGroupId stores the group and getActiveMcpGroupId reads it back`() =
        runTest {
            repository.setActiveMcpGroupId(userId = "user-1", groupId = "group-1")

            val result = repository.getActiveMcpGroupId("user-1")

            assertEquals("group-1", result)
        }

    @Test
    fun `setActiveMcpGroupId overwrites the previously stored group for the same user`() =
        runTest {
            repository.setActiveMcpGroupId(userId = "user-1", groupId = "group-1")
            repository.setActiveMcpGroupId(userId = "user-1", groupId = "group-2")

            val result = repository.getActiveMcpGroupId("user-1")

            assertEquals("group-2", result)
        }

    @Test
    fun `setActiveMcpGroupId keeps the active group per user separate`() =
        runTest {
            repository.setActiveMcpGroupId(userId = "user-1", groupId = "group-1")
            repository.setActiveMcpGroupId(userId = "user-2", groupId = "group-2")

            assertEquals("group-1", repository.getActiveMcpGroupId("user-1"))
            assertEquals("group-2", repository.getActiveMcpGroupId("user-2"))
        }

    @Test
    fun `setActiveMcpGroupId does not create a second document when overwriting`() =
        runTest {
            repository.setActiveMcpGroupId(userId = "user-1", groupId = "group-1")
            repository.setActiveMcpGroupId(userId = "user-1", groupId = "group-2")

            assertEquals(1, collection.countDocuments())
        }
}
