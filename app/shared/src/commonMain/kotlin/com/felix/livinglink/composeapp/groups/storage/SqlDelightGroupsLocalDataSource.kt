package com.felix.livinglink.composeapp.groups.storage

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.felix.livinglink.composeapp.core.db.DatabaseProvider
import com.felix.livinglink.composeapp.core.domain.LocalDataCleaner
import com.felix.livinglink.composeapp.groups.domain.Group
import com.felix.livinglink.composeapp.groups.domain.GroupsLocalDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single

@Single(binds = [GroupsLocalDataSource::class, LocalDataCleaner::class])
class SqlDelightGroupsLocalDataSource(
    private val databaseProvider: DatabaseProvider,
) : GroupsLocalDataSource, LocalDataCleaner {

    override fun observe(): Flow<List<Group>?> = flow {
        val q = databaseProvider.get().groupsQueries
        val groups = q.selectAllGroups().asFlow().mapToList(Dispatchers.Default)
        val loaded = q.selectLoaded().asFlow().mapToOneOrNull(Dispatchers.Default)
        emitAll(
            combine(groups, loaded) { rows, isLoaded ->
                when {
                    rows.isNotEmpty() -> rows.map { Group(id = it.id, name = it.name) }
                    isLoaded == true -> emptyList()
                    else -> null
                }
            }
        )
    }

    override suspend fun replaceAll(groups: List<Group>) = withContext(Dispatchers.Default) {
        val q = databaseProvider.get().groupsQueries
        q.transaction {
            q.deleteAllGroups()
            groups.forEachIndexed { i, g -> q.insertGroup(
                id = g.id,
                name = g.name,
                sortIndex = i.toLong())
            }
            q.ensureMeta()
            q.setLoaded(true)
        }
    }

    override fun observeSelectedGroupId(): Flow<String?> = flow {
        val q = databaseProvider.get().groupsQueries
        emitAll(
            q.selectSelectedGroupId().asFlow().mapToOneOrNull(Dispatchers.Default)
                .map { it?.selectedGroupId }
        )
    }

    override suspend fun setSelectedGroupId(groupId: String) {
        withContext(Dispatchers.Default) {
            val q = databaseProvider.get().groupsQueries
            q.transaction {
                q.ensureMeta()
                q.setSelectedGroupId(groupId)
            }
        }
    }

    override suspend fun clearLocalData() = withContext(Dispatchers.Default) {
        val q = databaseProvider.get().groupsQueries
        q.transaction {
            q.deleteAllGroups()
            q.ensureMeta()
            q.resetMeta()
        }
    }
}