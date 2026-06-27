package com.felix.livinglink.composeapp.shoppingList.domain

import com.felix.livinglink.composeapp.shoppingList.data.ShoppingListPendingMutation
import kotlinx.coroutines.flow.Flow

interface ShoppingListSyncLocalDataStore {
    val synced: Flow<Boolean>

    val pending: Flow<List<ShoppingListPendingMutation>>

    suspend fun enqueue(mutation: ShoppingListPendingMutation)

    suspend fun snapshot(): List<ShoppingListPendingMutation>

    suspend fun remove(mutation: ShoppingListPendingMutation)
}