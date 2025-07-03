package felix.livinglink.eventSourcing.store

import felix.livinglink.common.store.createStore
import felix.livinglink.eventSourcing.repository.AggregateSnapshot
import felix.livinglink.eventSourcing.repository.CacheKey
import felix.livinglink.json
import io.github.xxfast.kstore.KStore
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


class AggregateDefaultStore : AggregateStore {
    private val store: KStore<Map<CacheKey, String>> = createStore(
        path = "aggregate",
        defaultValue = emptyMap()
    )

    override suspend fun <AGGREGATE> get(
        cacheKey: CacheKey,
        serializer: KSerializer<AGGREGATE>
    ): AggregateSnapshot<AGGREGATE>? {
        val storedJsonString = store.get()?.get(cacheKey)
        return runCatching {
            storedJsonString?.let {
                val snapshotSerializer = AggregateSnapshot.serializer(serializer)
                json.decodeFromString(snapshotSerializer, it)
            }
        }.getOrNull()
    }

    override suspend fun <AGGREGATE> store(
        cacheKey: CacheKey,
        serializer: KSerializer<AGGREGATE>,
        snapshot: Any
    ) {
        val snapshotSerializer = AggregateSnapshot.serializer(serializer)

        @Suppress("UNCHECKED_CAST")
        val serialized = json.encodeToString(
            serializer = snapshotSerializer,
            value = snapshot as AggregateSnapshot<AGGREGATE>
        )

        store.update { current ->
            (current ?: emptyMap()) + (cacheKey to serialized)
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