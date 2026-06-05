package com.felix.livinglink.server.core.infrastructure.mongo

import com.felix.livinglink.server.core.domain.DeleteResult
import com.felix.livinglink.server.core.domain.UpdateOperationResult
import com.felix.livinglink.server.core.domain.UpdateResult
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.bson.codecs.pojo.annotations.BsonId
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MongoCrudMongoRepositoryTest : AbstractMongoRepositoryTest() {
    private lateinit var collection: MongoCollection<FakeLockingDocument>
    private lateinit var repository: MongoCrudRepository<FakeLockingDocument>

    @BeforeTest
    fun setUpCollection() {
        collection = database.getCollection<FakeLockingDocument>("fake_locking_items")
        runBlocking {
            collection.drop()
        }

        repository =
            MongoCrudRepository(collection, "FakeLockingDocument", maxOptimisticLockAttempts = 3)
    }

    @Test
    fun `should create document with initial version 0`() =
        runTest {
            val document = FakeLockingDocument(id = "create-id", name = "New Document", version = 99L)

            val created = repository.create(document)

            val expected = FakeLockingDocument(id = "create-id", name = "New Document", version = 0L)
            assertEquals(expected, created)
            assertEquals(expected, repository.findById("create-id"))
        }

    @Test
    fun `should find document by id or return null if it does not exist`() =
        runTest {
            assertNull(repository.findById("non-existent-id"))

            val existingDoc = FakeLockingDocument(id = "find-id", name = "Existing", version = 0L)
            repository.create(existingDoc)

            assertEquals(existingDoc, repository.findById("find-id"))
        }

    @Test
    fun `should recover and update successfully via retry when concurrent update occurs`() =
        runTest {
            val initialDoc = FakeLockingDocument(id = "test-id", name = "Original", version = 0L)
            repository.create(initialDoc)

            val sideEffects =
                ArrayDeque<suspend () -> Unit>().apply {
                    add {
                        repository.updateWithOptimisticLocking("test-id") { doc2 ->
                            UpdateOperationResult.updated(doc2.copy(name = doc2.name + ", Changed by A"))
                        }
                    }
                }

            val result =
                repository.updateWithOptimisticLocking("test-id") { doc ->
                    runBlocking {
                        sideEffects.removeFirstOrNull()?.invoke()
                    }
                    UpdateOperationResult.updated(doc.copy(name = doc.name + ", Changed by B"))
                }

            val expectedDoc = FakeLockingDocument(id = "test-id", name = "Original, Changed by A, Changed by B", version = 2L)
            val expectedResponse = FakeLockingDocument(id = "test-id", name = "Original, Changed by A, Changed by B", version = 1L)

            assertEquals(UpdateResult.Updated(expectedDoc, expectedResponse), result)
        }

    @Test
    fun `should fail with optimistic locking error when max attempts are exceeded`() =
        runTest {
            val initialDoc = FakeLockingDocument(id = "retry-id", name = "Original", version = 0L)
            repository.create(initialDoc)

            val sideEffects =
                ArrayDeque<suspend () -> Unit>().apply {
                    repeat(5) { index ->
                        add {
                            repository.updateWithOptimisticLocking("retry-id") { doc2 ->
                                UpdateOperationResult.updated(doc2.copy(name = doc2.name + ", Interruption $index"))
                            }
                        }
                    }
                }

            val result =
                repository.updateWithOptimisticLocking("retry-id") { doc ->
                    runBlocking {
                        sideEffects.removeFirstOrNull()?.invoke()
                    }
                    UpdateOperationResult.updated(doc.copy(name = doc.name + ", Main Update"))
                }

            assertEquals(UpdateResult.OptimisticLockingError, result)
        }

    @Test
    fun `should delete document by id`() =
        runTest {
            val documentToDelete =
                FakeLockingDocument(
                    id = "delete-id",
                    name = "New Document",
                    version = 0L,
                )

            val documentToNotDelete =
                FakeLockingDocument(
                    id = "not-delete-id",
                    name = "New Document",
                    version = 0L,
                )

            repository.create(documentToDelete)
            repository.create(documentToNotDelete)

            val deleteResult = repository.deleteById("delete-id")
            assertEquals(DeleteResult.Deleted, deleteResult)
            assertNull(repository.findById("delete-id"))
            assertEquals(documentToNotDelete, repository.findById("not-delete-id"))
        }

    @Test
    fun `should return NotFound when deleting non existing document`() =
        runTest {
            val documentToNotDelete =
                FakeLockingDocument(
                    id = "not-delete-id",
                    name = "New Document",
                    version = 0L,
                )

            repository.create(documentToNotDelete)

            val result = repository.deleteById("does-not-exist")

            assertEquals(DeleteResult.NotFound, result)
            assertEquals(documentToNotDelete, repository.findById("not-delete-id"))
        }
}

data class FakeLockingDocument(
    @param:BsonId override val id: String,
    val name: String,
    override val version: Long,
) : MongoVersionedDocument<FakeLockingDocument> {
    override fun withVersion(version: Long): FakeLockingDocument = copy(version = version)
}
