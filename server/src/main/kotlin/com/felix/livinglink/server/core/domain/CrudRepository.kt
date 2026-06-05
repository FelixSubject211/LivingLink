package com.felix.livinglink.server.core.domain

interface CrudRepository<T> {
    suspend fun create(value: T): T

    suspend fun findById(id: String): T?

    suspend fun deleteById(id: String): DeleteResult

    suspend fun <TResponse> updateWithOptimisticLocking(
        id: String,
        modify: (T) -> UpdateOperationResult<T, TResponse>,
    ): UpdateResult<T, TResponse>
}

sealed class UpdateOperationResult<out T, out TResponse> {
    data class Updated<T, TResponse>(
        val newEntity: T,
        val response: TResponse,
    ) : UpdateOperationResult<T, TResponse>()

    data class NoUpdate<TResponse>(
        val response: TResponse,
    ) : UpdateOperationResult<Nothing, TResponse>()

    companion object {
        fun <T> updated(newEntity: T): UpdateOperationResult<T, T> =
            Updated(newEntity = newEntity, response = newEntity)

        fun <T> noUpdate(current: T): UpdateOperationResult<Nothing, T> =
            NoUpdate(response = current)
    }
}

sealed class UpdateResult<out T, out TResponse> {
    data class Updated<T, TResponse>(
        val newEntity: T,
        val response: TResponse,
    ) : UpdateResult<T, TResponse>()

    data class NotUpdated<TResponse>(
        val response: TResponse,
    ) : UpdateResult<Nothing, TResponse>()

    data object OptimisticLockingError : UpdateResult<Nothing, Nothing>()

    data object NotFound : UpdateResult<Nothing, Nothing>()
}

sealed class DeleteResult {
    data object Deleted : DeleteResult()

    data object NotFound : DeleteResult()
}
