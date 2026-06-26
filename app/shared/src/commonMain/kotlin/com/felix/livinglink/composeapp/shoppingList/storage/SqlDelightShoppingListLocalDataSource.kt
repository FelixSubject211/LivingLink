package com.felix.livinglink.composeapp.shoppingList.storage

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.felix.livinglink.composeapp.core.db.DatabaseProvider
import com.felix.livinglink.composeapp.core.domain.GroupScopedDataCleaner
import com.felix.livinglink.composeapp.core.domain.LogoutDataCleaner
import com.felix.livinglink.composeapp.db.ShoppingListItemEntity
import com.felix.livinglink.composeapp.shoppingList.domain.ItemSuggestion
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListContent
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListItem
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListLocalDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import kotlin.time.Instant

@Single(binds = [
    ShoppingListLocalDataSource::class,
    LogoutDataCleaner::class,
    GroupScopedDataCleaner::class,
])
class SqlDelightShoppingListLocalDataSource(
    private val databaseProvider: DatabaseProvider,
) : ShoppingListLocalDataSource, LogoutDataCleaner, GroupScopedDataCleaner {

    override fun observe(groupId: String): Flow<ShoppingListContent?> = flow {
        val q = databaseProvider.get().shoppingListQueries
        val items = q.selectItems(groupId).asFlow().mapToList(Dispatchers.Default)
        val meta = q.selectMeta(groupId).asFlow().mapToOneOrNull(Dispatchers.Default)
        emitAll(
            combine(items, meta) { rows, totalCount ->
                if (totalCount == null) null else buildContent(rows, totalCount.toInt())
            }
        )
    }

    override suspend fun putRange(
        groupId: String,
        fromIndex: Int,
        items: List<ShoppingListItem>,
        totalCount: Int,
    ) = withContext(Dispatchers.Default) {
        val q = databaseProvider.get().shoppingListQueries
        q.transaction {
            q.upsertMeta(groupId, totalCount.toLong())
            q.deleteWindow(groupId, fromIndex.toLong(), (fromIndex + items.size).toLong())
            items.forEach { q.deleteById(groupId, it.id) }
            items.forEachIndexed { i, item ->
                q.insertItem(
                    groupId = groupId,
                    position = (fromIndex + i).toLong(),
                    id = item.id,
                    name = item.name,
                    completed = item.completed,
                    createdByUserId = item.createdByUserId,
                    createdAt = item.createdAt.toEpochMilliseconds(),
                    updatedAt = item.updatedAt.toEpochMilliseconds(),
                )
            }
            q.deleteRowsFromIndex(groupId, totalCount.toLong())
        }
    }

    override suspend fun updateItem(
        groupId: String,
        itemId: String,
        transform: (ShoppingListItem) -> ShoppingListItem,
    ) = withContext(Dispatchers.Default) {
        val q = databaseProvider.get().shoppingListQueries
        q.transaction {
            val existing = q.selectItemById(groupId, itemId).awaitAsOneOrNull()
                ?: return@transaction
            val updated = transform(existing.toDomain())
            q.updateItemById(
                groupId = groupId,
                id = itemId,
                name = updated.name,
                completed = updated.completed,
                createdByUserId = updated.createdByUserId,
                createdAt = updated.createdAt.toEpochMilliseconds(),
                updatedAt = updated.updatedAt.toEpochMilliseconds(),
            )
        }
    }

    override suspend fun removeItem(groupId: String, itemId: String) =
        withContext(Dispatchers.Default) {
            val q = databaseProvider.get().shoppingListQueries
            q.transaction {
                val pos = q.selectPositionById(groupId, itemId).awaitAsOneOrNull()
                    ?: return@transaction
                q.deleteItemById(groupId, itemId)
                q.shiftPositionsDownStep1(groupId, pos)
                q.shiftPositionsDownStep2(groupId)
                q.decrementMeta(groupId)
            }
        }

    override fun observeSuggestions(
        groupId: String,
        query: String,
    ): Flow<List<ItemSuggestion>> = flow {
        val q = databaseProvider.get().shoppingListQueries
        val normalizedQuery = query.trim().lowercase()
        emitAll(
            q.selectNameSuggestions(
                groupId = groupId,
                query = normalizedQuery,
                limit = SUGGESTIONS_LIMIT,
            )
                .asFlow()
                .mapToList(Dispatchers.Default)
                .map { rows ->
                    rows.map { row ->
                        ItemSuggestion(
                            name = row.name,
                            usageCount = row.usageCount.toInt(),
                        )
                    }
                }
        )
    }

    override suspend fun deleteGroups(groupIds: Set<String>) =
        withContext(Dispatchers.Default) {
            if (groupIds.isEmpty()) return@withContext
            val q = databaseProvider.get().shoppingListQueries
            q.transaction {
                q.deleteItemsForGroups(groupIds.toList())
                q.deleteMetaForGroups(groupIds.toList())
            }
        }

    override suspend fun clearLocalData() = withContext(Dispatchers.Default) {
        val q = databaseProvider.get().shoppingListQueries
        q.transaction {
            q.deleteAllItems()
            q.deleteAllMeta()
        }
    }

    private fun buildContent(rows: List<ShoppingListItemEntity>, totalCount: Int): ShoppingListContent {
        val order = arrayOfNulls<String>(totalCount)
        val itemsById = HashMap<String, ShoppingListItem>(rows.size)
        rows.forEach { row ->
            val pos = row.position.toInt()
            if (pos in 0 until totalCount) order[pos] = row.id
            itemsById[row.id] = row.toDomain()
        }
        return ShoppingListContent(itemsById = itemsById, order = order.toList())
    }

    private companion object {
        const val SUGGESTIONS_LIMIT = 50L
    }
}

private fun ShoppingListItemEntity.toDomain() = ShoppingListItem(
    id = id,
    name = name,
    completed = completed,
    createdByUserId = createdByUserId,
    createdAt = Instant.fromEpochMilliseconds(createdAt),
    updatedAt = Instant.fromEpochMilliseconds(updatedAt),
)