package felix.livinglink.eventSourcing.store

import felix.livinglink.common.store.createStore
import felix.livinglink.eventSourcing.repository.CacheKey
import felix.livinglink.json
import io.github.xxfast.kstore.KStore
import kotlinx.serialization.KSerializer

interface AggregateStore {
    suspend fun <AGGREGATE> get(
        cacheKey: CacheKey,
        serializer: KSerializer<AGGREGATE>
    ): AGGREGATE?

    suspend fun <AGGREGATE> store(
        cacheKey: CacheKey,
        serializer: KSerializer<AGGREGATE>,
        aggregate: Any
    )

    suspend fun clear(cacheKey: CacheKey)

    suspend fun clearAll()
}

class AggregateDefaultStore : AggregateStore {
    private val store: KStore<Map<CacheKey, String>> = createStore(
        path = "aggregate",
        defaultValue = emptyMap()
    )

    override suspend fun <AGGREGATE> get(
        cacheKey: CacheKey,
        serializer: KSerializer<AGGREGATE>
    ): AGGREGATE? {
        val storedJsonString = store.get()?.get(cacheKey)
        return runCatching {
            storedJsonString?.let { json.decodeFromString(serializer, it) }
        }.getOrNull()
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun <AGGREGATE> store(
        cacheKey: CacheKey,
        serializer: KSerializer<AGGREGATE>,
        aggregate: Any
    ) {
        val serializedAggregate = json.encodeToString(serializer, aggregate as AGGREGATE)
        store.update { current ->
            (current ?: emptyMap()) + (cacheKey to serializedAggregate)
        }
    }

    override suspend fun clear(cacheKey: CacheKey) {
        store.update { current ->
            current?.minus(cacheKey)
        }
    }

    override suspend fun clearAll() {
        store.update { emptyMap() }
    }
}