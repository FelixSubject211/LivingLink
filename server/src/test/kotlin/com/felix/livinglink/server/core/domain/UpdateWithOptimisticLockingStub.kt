package com.felix.livinglink.server.core.domain

import com.felix.livinglink.server.core.domain.CrudRepository
import com.felix.livinglink.server.core.domain.UpdateOperationResult
import com.felix.livinglink.server.core.domain.UpdateResult
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any

inline fun <reified T : Any> CrudRepository<T>.stubUpdates(
    id: String,
    currentItem: T,
) {
    everySuspend {
        updateWithOptimisticLocking<T>(
            id = id,
            modify = any(),
        )
    } calls { (_: String, modify: (T) -> UpdateOperationResult<T, T>) ->
        val op = modify(currentItem) as UpdateOperationResult.Updated<T, T>
        UpdateResult.Updated(newEntity = op.newEntity, response = op.response)
    }
}

inline fun <reified T : Any> CrudRepository<T>.stubDoesNotUpdate(
    id: String,
    currentItem: T,
) {
    everySuspend {
        updateWithOptimisticLocking<T>(
            id = id,
            modify = any(),
        )
    } calls { (_: String, modify: (T) -> UpdateOperationResult<T, T>) ->
        val op = modify(currentItem) as UpdateOperationResult.NoUpdate<T>
        UpdateResult.NotUpdated(response = op.response)
    }
}

inline fun <reified T : Any> CrudRepository<T>.stubNotFound(id: String) {
    everySuspend {
        updateWithOptimisticLocking<T>(
            id = id,
            modify = any(),
        )
    } returns UpdateResult.NotFound
}

inline fun <reified T : Any> CrudRepository<T>.stubConflict(id: String) {
    everySuspend {
        updateWithOptimisticLocking<T>(
            id = id,
            modify = any(),
        )
    } returns UpdateResult.OptimisticLockingError
}
