package com.felix.livinglink.composeapp.shoppingList.data

import com.felix.livinglink.composeapp.core.domain.Loadable
import com.felix.livinglink.composeapp.groups.domain.GroupsRepository
import com.felix.livinglink.composeapp.shoppingList.domain.ItemSuggestion
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListContent
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListRepository
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListSyncLocalDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single(binds = [ShoppingListRepository::class])
class OfflineFirstShoppingListRepository(
    @Named("base") private val delegate: ShoppingListRepository,
    private val shoppingListSyncLocalDataStore: ShoppingListSyncLocalDataStore,
    private val groupRepository: GroupsRepository,
) : ShoppingListRepository {

    override val state: Flow<Loadable<ShoppingListContent>> =
        combine(delegate.state, shoppingListSyncLocalDataStore.pending) { loadable, pending ->
            when (loadable) {
                is Loadable.Content ->
                    loadable.copy(
                        value = loadable.value.applyPending(pending),
                        synced = pending.isEmpty(),
                    )
                else -> loadable
            }
        }

    override fun setVisibleRange(firstVisibleIndex: Int, lastVisibleIndex: Int) {
        delegate.setVisibleRange(firstVisibleIndex, lastVisibleIndex)
    }

    override suspend fun addItem(name: String): ShoppingListRepository.AddResult {
        return delegate.addItem(name)
    }

    override suspend fun changeItemCompleteState(
        itemId: String,
        completed: Boolean,
    ): ShoppingListRepository.ChangeCompleteStateResult {
        val result = delegate.changeItemCompleteState(itemId = itemId, completed = completed)
        if (result == ShoppingListRepository.ChangeCompleteStateResult.NetworkError) {
            shoppingListSyncLocalDataStore.enqueue(ShoppingListPendingMutation.CompleteChange(
                groupId = groupRepository.selectedGroupId.firstOrNull() ?: return result,
                itemId = itemId,
                completed = completed
            ))
            return ShoppingListRepository.ChangeCompleteStateResult.Success
        }
        return result
    }

    override suspend fun deleteItem(itemId: String): ShoppingListRepository.DeleteResult {
        val result = delegate.deleteItem(itemId)
        if (result == ShoppingListRepository.DeleteResult.NetworkError) {
            shoppingListSyncLocalDataStore.enqueue(ShoppingListPendingMutation.Delete(
                groupId = groupRepository.selectedGroupId.firstOrNull() ?: return result,
                itemId = itemId
            ))
            return ShoppingListRepository.DeleteResult.Success
        }
        return result
    }

    override fun observeSuggestions(query: String): Flow<List<ItemSuggestion>> =
        delegate.observeSuggestions(query)
}

private fun ShoppingListContent.applyPending(
    pending: List<ShoppingListPendingMutation>,
): ShoppingListContent {
    if (pending.isEmpty()) return this

    val deletedIds = pending
        .filterIsInstance<ShoppingListPendingMutation.Delete>()
        .mapTo(mutableSetOf()) { it.itemId }

    val completedById = pending
        .filterIsInstance<ShoppingListPendingMutation.CompleteChange>()
        .associate { it.itemId to it.completed }

    val patchedItems = itemsById.mapValues { (id, item) ->
        completedById[id]?.let { item.copy(completed = it) } ?: item
    }.toMutableMap()

    val patchedOrder = order
        .filterNot { it != null && it in deletedIds }
        .toMutableList()

    val idsInOrder = patchedOrder.filterNotNull().toSet()
    val cleanedItems = patchedItems.filterKeys { it in idsInOrder }

    return ShoppingListContent(itemsById = cleanedItems, order = patchedOrder)
}