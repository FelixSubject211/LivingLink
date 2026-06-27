package com.felix.livinglink.composeapp.shoppingList.storage

import com.felix.livinglink.composeapp.shoppingList.data.ShoppingListPendingMutation
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListSyncLocalDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single

class InMemoryShoppingListSyncLocalDataStore : ShoppingListSyncLocalDataStore {
    private val _pending = MutableStateFlow<List<ShoppingListPendingMutation>>(emptyList())
    override val pending: Flow<List<ShoppingListPendingMutation>> = _pending.asStateFlow()

    override val synced: Flow<Boolean> = _pending.map { it.isEmpty() }

    override suspend fun enqueue(mutation: ShoppingListPendingMutation) {
        _pending.update { it + mutation }
    }

    override suspend fun snapshot(): List<ShoppingListPendingMutation> =
        _pending.value

    override suspend fun remove(mutation: ShoppingListPendingMutation) {
        _pending.update { list ->
            val index = list.indexOf(mutation)
            if (index < 0) list else list.toMutableList().apply { removeAt(index) }
        }
    }
}