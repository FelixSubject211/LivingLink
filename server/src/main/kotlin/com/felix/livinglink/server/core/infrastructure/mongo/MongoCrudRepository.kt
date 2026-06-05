package com.felix.livinglink.server.core.infrastructure.mongo

import com.felix.livinglink.server.core.domain.CrudRepository
import com.felix.livinglink.server.core.domain.DeleteResult
import com.felix.livinglink.server.core.domain.UpdateOperationResult
import com.felix.livinglink.server.core.domain.UpdateResult
import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Filters.eq
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.firstOrNull
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("MongoCrudRepository")

class MongoCrudRepository<TDocument : MongoVersionedDocument<TDocument>>(
    private val collection: MongoCollection<TDocument>,
    private val entityName: String,
    private val maxOptimisticLockAttempts: Int = 100,
) : CrudRepository<TDocument> {
    init {
        require(maxOptimisticLockAttempts > 0) {
            "maxOptimisticLockAttempts must be greater than 0."
        }
    }

    override suspend fun create(value: TDocument): TDocument {
        val document = value.withVersion(0)
        collection.insertOne(document)
        return document
    }

    override suspend fun findById(id: String): TDocument? =
        collection
            .find(eq("_id", id))
            .firstOrNull()

    override suspend fun deleteById(id: String): DeleteResult {
        val result = collection.deleteOne(eq("_id", id))

        return if (result.deletedCount == 0L) {
            DeleteResult.NotFound
        } else {
            DeleteResult.Deleted
        }
    }

    override suspend fun <TResponse> updateWithOptimisticLocking(
        id: String,
        modify: (TDocument) -> UpdateOperationResult<TDocument, TResponse>,
    ): UpdateResult<TDocument, TResponse> {
        repeat(maxOptimisticLockAttempts) {
            val current =
                findById(id)
                    ?: return UpdateResult.NotFound

            when (val operation = modify(current)) {
                is UpdateOperationResult.NoUpdate ->
                    return UpdateResult.NotUpdated(operation.response)

                is UpdateOperationResult.Updated -> {
                    val updatedDocument = operation.newEntity.withVersion(current.version + 1)

                    val result =
                        collection.replaceOne(
                            filter =
                                and(
                                    eq("_id", current.id),
                                    eq("version", current.version),
                                ),
                            replacement = updatedDocument,
                        )

                    if (result.modifiedCount == 1L) {
                        return UpdateResult.Updated(
                            newEntity = updatedDocument,
                            response = operation.response,
                        )
                    }

                    // Either the document disappeared, or someone else updated it.
                    // Loop again to re-read fresh state and re-apply modify.
                }
            }
        }

        logger.error("$entityName '$id' could not be updated after $maxOptimisticLockAttempts attempts.")
        return UpdateResult.OptimisticLockingError
    }
}
