package com.felix.livinglink.composeapp.shoppingList.storage

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.felix.livinglink.composeapp.core.db.DatabaseProvider
import com.felix.livinglink.composeapp.core.domain.GroupScopedDataCleaner
import com.felix.livinglink.composeapp.core.domain.LogoutDataCleaner
import com.felix.livinglink.composeapp.db.ShoppingListPendingMutation as ShoppingListPendingMutationEntity
import com.felix.livinglink.composeapp.shoppingList.data.ShoppingListPendingMutation
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListSyncLocalDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single

@Single(binds = [
    ShoppingListSyncLocalDataStore::class,
    LogoutDataCleaner::class,
    GroupScopedDataCleaner::class,
])
class SqlDelightShoppingListSyncLocalDataStore(
    private val databaseProvider: DatabaseProvider,
) : ShoppingListSyncLocalDataStore, LogoutDataCleaner, GroupScopedDataCleaner {

    private suspend fun queries() = databaseProvider.get().shoppingListSyncQueries

    override val pending: Flow<List<ShoppingListPendingMutation>> = flow {
        val q = queries()
        emitAll(
            q.selectAll().asFlow().mapToList(Dispatchers.Default)
                .map { rows -> rows.map { it.toDomain() } }
        )
    }

    override val synced: Flow<Boolean> = pending.map { it.isEmpty() }

    override suspend fun enqueue(mutation: ShoppingListPendingMutation) =
        withContext(Dispatchers.Default) {
            val q = queries()
            when (mutation) {
                is ShoppingListPendingMutation.CompleteChange ->
                    q.insert(
                        type = TYPE_COMPLETE_CHANGE,
                        groupId = mutation.groupId,
                        itemId = mutation.itemId,
                        completed = mutation.completed,
                    )

                is ShoppingListPendingMutation.Delete ->
                    q.insert(
                        type = TYPE_DELETE,
                        groupId = mutation.groupId,
                        itemId = mutation.itemId,
                        completed = null,
                    )
            }
            Unit
        }

    override suspend fun snapshot(): List<ShoppingListPendingMutation> =
        withContext(Dispatchers.Default) {
            queries().selectAll().awaitAsList().map { it.toDomain() }
        }

    override suspend fun remove(mutation: ShoppingListPendingMutation) =
        withContext(Dispatchers.Default) {
            val q = queries()
            val sequence = when (mutation) {
                is ShoppingListPendingMutation.CompleteChange ->
                    q.selectSequenceForCompleteChange(
                        groupId = mutation.groupId,
                        itemId = mutation.itemId,
                        completed = mutation.completed,
                    ).awaitAsOneOrNull()

                is ShoppingListPendingMutation.Delete ->
                    q.selectSequenceForDelete(
                        groupId = mutation.groupId,
                        itemId = mutation.itemId,
                    ).awaitAsOneOrNull()
            }
            sequence?.let { q.deleteBySequence(it) }
            Unit
        }

    override suspend fun clearLocalData() = withContext(Dispatchers.Default) {
        queries().deleteAll()
        Unit
    }

    override suspend fun deleteGroups(groupIds: Set<String>) =
        withContext(Dispatchers.Default) {
            if (groupIds.isEmpty()) return@withContext
            queries().deleteForGroups(groupIds.toList())
        }

    private fun ShoppingListPendingMutationEntity.toDomain(): ShoppingListPendingMutation =
        when (type) {
            TYPE_COMPLETE_CHANGE ->
                ShoppingListPendingMutation.CompleteChange(
                    groupId = groupId,
                    itemId = itemId,
                    completed = completed
                        ?: error("CompleteChange row $sequence has null completed"),
                )

            TYPE_DELETE ->
                ShoppingListPendingMutation.Delete(
                    groupId = groupId,
                    itemId = itemId,
                )

            else -> error("Unknown pending mutation type: $type")
        }

    private companion object {
        const val TYPE_COMPLETE_CHANGE = "CompleteChange"
        const val TYPE_DELETE = "Delete"
    }
}