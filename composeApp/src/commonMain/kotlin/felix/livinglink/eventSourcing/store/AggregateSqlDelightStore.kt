package felix.livinglink.eventSourcing.store

import app.cash.sqldelight.db.SqlDriver
import felix.livinglink.db.AppDatabase
import felix.livinglink.eventSourcing.repository.AggregateSnapshot
import felix.livinglink.eventSourcing.repository.CacheKey
import felix.livinglink.json
import kotlinx.serialization.KSerializer

class AggregateSqlDelightStore(
    driver: SqlDriver
) : AggregateStore {

    private val queries = AppDatabase(driver).aggregateQueries

    override suspend fun <AGGREGATE> get(
        cacheKey: CacheKey,
        serializer: KSerializer<AGGREGATE>
    ): AggregateSnapshot<AGGREGATE>? {
        val row = queries.getAggregate(
            groupId = cacheKey.groupId,
            aggregationKey = cacheKey.aggregationKey
        ).executeAsOneOrNull() ?: return null

        val snapshotSerializer = AggregateSnapshot.serializer(serializer)
        return json.decodeFromString(snapshotSerializer, row.snapshot_json)
    }

    override suspend fun <AGGREGATE> store(
        cacheKey: CacheKey,
        serializer: KSerializer<AGGREGATE>,
        snapshot: Any
    ) {
        @Suppress("UNCHECKED_CAST")
        val typedSnapshot = snapshot as AggregateSnapshot<AGGREGATE>
        val snapshotSerializer = AggregateSnapshot.serializer(serializer)
        val jsonString = json.encodeToString(snapshotSerializer, typedSnapshot)

        queries.insertAggregate(
            group_id = cacheKey.groupId,
            aggregation_key = cacheKey.aggregationKey,
            last_seen_global_event_id = typedSnapshot.lastSeenGlobalEventId,
            snapshot_json = jsonString
        )
    }

    override suspend fun clear(cacheKey: CacheKey) {
        queries.clear(
            groupId = cacheKey.groupId,
            aggregationKey = cacheKey.aggregationKey
        )
    }

    override suspend fun clearAll() {
        queries.clearAll()
    }
}
