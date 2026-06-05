package com.felix.livinglink.server.core.domain

class MappedCrudRepository<TDomain, TStorage>(
    private val storageRepository: CrudRepository<TStorage>,
    private val toStorage: (TDomain) -> TStorage,
    private val toDomain: (TStorage) -> TDomain,
) : CrudRepository<TDomain> {
    override suspend fun create(value: TDomain): TDomain =
        storageRepository
            .create(toStorage(value))
            .let(toDomain)

    override suspend fun findById(id: String): TDomain? =
        storageRepository
            .findById(id)
            ?.let(toDomain)

    override suspend fun deleteById(id: String): DeleteResult =
        storageRepository
            .deleteById(id)

    override suspend fun <TResponse> updateWithOptimisticLocking(
        id: String,
        modify: (TDomain) -> UpdateOperationResult<TDomain, TResponse>,
    ): UpdateResult<TDomain, TResponse> {
        val storageResult =
            storageRepository.updateWithOptimisticLocking(id) { storageCurrent ->
                val domainCurrent = toDomain(storageCurrent)
                when (val operation = modify(domainCurrent)) {
                    is UpdateOperationResult.NoUpdate ->
                        UpdateOperationResult.NoUpdate(operation.response)

                    is UpdateOperationResult.Updated ->
                        UpdateOperationResult.Updated(
                            newEntity = toStorage(operation.newEntity),
                            response = operation.response,
                        )
                }
            }

        return when (storageResult) {
            is UpdateResult.NotFound ->
                UpdateResult.NotFound

            is UpdateResult.NotUpdated ->
                UpdateResult.NotUpdated(storageResult.response)

            is UpdateResult.OptimisticLockingError ->
                UpdateResult.OptimisticLockingError

            is UpdateResult.Updated ->
                UpdateResult.Updated(
                    newEntity = toDomain(storageResult.newEntity),
                    response = storageResult.response,
                )
        }
    }
}
