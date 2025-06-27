package felix.livinglink.eventSourcing.store

import felix.livinglink.common.store.createStore
import felix.livinglink.json
import io.github.xxfast.kstore.KStore
import kotlinx.serialization.KSerializer

interface AggregateStore {
    suspend fun <A> get(cacheKey: String, serializer: KSerializer<A>): A?

    suspend fun <A> store(cacheKey: String, serializer: KSerializer<A>, aggregate: A)

    suspend fun clearAll()
}

class AggregateDefaultStore : AggregateStore {
    private val store: KStore<Map<String, String>> = createStore(
        path = "aggregates",
        defaultValue = emptyMap()
    )

    override suspend fun <A> get(cacheKey: String, serializer: KSerializer<A>): A? {
        val storedJsonString = store.get()?.get(cacheKey)
        return runCatching {
            storedJsonString?.let { json.decodeFromString(serializer, it) }
        }.getOrNull()
    }

    override suspend fun <A> store(cacheKey: String, serializer: KSerializer<A>, aggregate: A) {
        val serializedAggregate = json.encodeToString(serializer, aggregate)
        store.update { current ->
            (current ?: emptyMap()) + (cacheKey to serializedAggregate)
        }
    }

    override suspend fun clearAll() {
        store.update { emptyMap() }
    }
}