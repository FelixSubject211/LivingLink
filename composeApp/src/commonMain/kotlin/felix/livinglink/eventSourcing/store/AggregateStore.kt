package felix.livinglink.eventSourcing.store

import felix.livinglink.eventSourcing.repository.AggregateSnapshot
import felix.livinglink.eventSourcing.repository.CacheKey
import kotlinx.serialization.KSerializer

interface AggregateStore {
    suspend fun <AGGREGATE> get(
        cacheKey: CacheKey,
        serializer: KSerializer<AGGREGATE>
    ): AggregateSnapshot<AGGREGATE>?

    suspend fun <AGGREGATE> store(
        cacheKey: CacheKey,
        serializer: KSerializer<AGGREGATE>,
        snapshot: Any
    )

    suspend fun clear(cacheKey: CacheKey)

    suspend fun clearAll()
}

expect class AggregateDefaultStore() : AggregateStore
