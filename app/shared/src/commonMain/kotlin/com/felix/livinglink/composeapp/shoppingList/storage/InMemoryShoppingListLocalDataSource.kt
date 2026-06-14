package com.felix.livinglink.composeapp.shoppingList.storage

import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListContent
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListItem
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListLocalDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Single

@Single(binds = [ShoppingListLocalDataSource::class])
class InMemoryShoppingListLocalDataSource : ShoppingListLocalDataSource {

    private val mutex = Mutex()
    private val groups = mutableMapOf<String, MutableStateFlow<ShoppingListContent?>>()

    override fun observe(groupId: String): Flow<ShoppingListContent?> =
        flow { emitAll(stateOf(groupId)) }

    override suspend fun putRange(
        groupId: String,
        fromIndex: Int,
        items: List<ShoppingListItem>,
        totalCount: Int,
    ) {
        stateOf(groupId).update { current -> current.merge(fromIndex, items, totalCount) }
    }

    override suspend fun updateItem(
        groupId: String,
        itemId: String,
        transform: (ShoppingListItem) -> ShoppingListItem,
    ) {
        stateOf(groupId).update { current ->
            val existing = current?.itemsById?.get(itemId) ?: return@update current
            current.copy(
                itemsById = current.itemsById + (itemId to transform(existing)),
            )
        }
    }

    override suspend fun retainGroups(groupIds: Set<String>) {
        mutex.withLock {
            groups.keys.retainAll(groupIds)
        }
    }

    private suspend fun stateOf(groupId: String): MutableStateFlow<ShoppingListContent?> =
        mutex.withLock {
            groups.getOrPut(groupId) { MutableStateFlow(null) }
        }

    private fun ShoppingListContent?.merge(
        fromIndex: Int,
        items: List<ShoppingListItem>,
        totalCount: Int,
    ): ShoppingListContent {
        val freshIds = items.mapTo(mutableSetOf()) { it.id }
        val freshRange = fromIndex until (fromIndex + items.size)

        val order = List(totalCount) { index ->
            when {
                index in freshRange -> items[index - fromIndex].id
                else -> this?.order?.getOrNull(index)?.takeUnless { it in freshIds }
            }
        }

        val idsInOrder = order.filterNotNullTo(mutableSetOf())
        val itemsById = buildMap {
            this@merge?.itemsById?.forEach { (id, item) ->
                if (id in idsInOrder) put(id, item)
            }
            items.forEach { put(it.id, it) }
        }

        return ShoppingListContent(itemsById = itemsById, order = order)
    }
}