package com.felix.livinglink.server.shoppingList.application

import com.felix.livinglink.server.core.domain.UpdateOperationResult
import com.felix.livinglink.server.core.domain.UpdateResult
import com.felix.livinglink.server.group.application.RequireGroupMembershipUseCase
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItem
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItemRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.koin.core.annotation.Single
import kotlin.time.Instant

@Single
class ChangeShoppingListItemsCompleteStateUseCase(
    private val shoppingListItemRepository: ShoppingListItemRepository,
    private val requireGroupMembershipUseCase: RequireGroupMembershipUseCase,
) {
    suspend operator fun invoke(input: Input): Output {
        requireGroupMembershipUseCase(userId = input.byUserId, groupId = input.groupId)

        return coroutineScope {
            val results =
                input.changes
                    .map { change ->
                        async {
                            val existing = shoppingListItemRepository.findById(change.itemId)
                            if (existing == null || existing.groupId != input.groupId) {
                                ItemResult.Missing(change.itemId)
                            } else {
                                applyChange(
                                    id = change.itemId,
                                    completed = change.completed,
                                    byUserId = input.byUserId,
                                    at = change.at,
                                )
                            }
                        }
                    }.awaitAll()

            val changedItems = mutableListOf<ShoppingListItem>()
            val alreadyChangedItems = mutableListOf<ShoppingListItem>()
            val missingIds = mutableListOf<String>()
            val conflictedIds = mutableListOf<String>()

            results.forEach { result ->
                when (result) {
                    is ItemResult.Changed -> changedItems += result.item
                    is ItemResult.AlreadyChanged -> alreadyChangedItems += result.item
                    is ItemResult.Missing -> missingIds += result.id
                    is ItemResult.Conflict -> conflictedIds += result.id
                }
            }

            Output(
                changedItems = changedItems,
                alreadyChangedItems = alreadyChangedItems,
                missingIds = missingIds,
                conflictedIds = conflictedIds,
            )
        }
    }

    private suspend fun applyChange(
        id: String,
        completed: Boolean,
        byUserId: String,
        at: Instant,
    ): ItemResult {
        val result =
            shoppingListItemRepository.updateWithOptimisticLocking(id) { current ->
                when (completed) {
                    true ->
                        if (current.isCompleted) {
                            UpdateOperationResult.noUpdate(current = current)
                        } else {
                            UpdateOperationResult.updated(
                                newEntity = current.complete(byUserId = byUserId, at = at),
                            )
                        }
                    false ->
                        if (!current.isCompleted) {
                            UpdateOperationResult.noUpdate(current = current)
                        } else {
                            UpdateOperationResult.updated(
                                newEntity = current.unComplete(byUserId = byUserId, at = at),
                            )
                        }
                }
            }

        return when (result) {
            is UpdateResult.NotFound -> ItemResult.Missing(id)
            is UpdateResult.OptimisticLockingError -> ItemResult.Conflict(id)
            is UpdateResult.NotUpdated -> ItemResult.AlreadyChanged(result.response)
            is UpdateResult.Updated -> ItemResult.Changed(result.newEntity)
        }
    }

    private sealed class ItemResult {
        data class Changed(
            val item: ShoppingListItem,
        ) : ItemResult()

        data class AlreadyChanged(
            val item: ShoppingListItem,
        ) : ItemResult()

        data class Missing(
            val id: String,
        ) : ItemResult()

        data class Conflict(
            val id: String,
        ) : ItemResult()
    }

    data class Input(
        val byUserId: String,
        val groupId: String,
        val changes: List<Change>,
    )

    data class Change(
        val itemId: String,
        val completed: Boolean,
        val at: Instant,
    )

    data class Output(
        val changedItems: List<ShoppingListItem>,
        val alreadyChangedItems: List<ShoppingListItem>,
        val missingIds: List<String>,
        val conflictedIds: List<String>,
    )
}
