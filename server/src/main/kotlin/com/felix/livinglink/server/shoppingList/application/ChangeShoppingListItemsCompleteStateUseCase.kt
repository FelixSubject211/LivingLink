package com.felix.livinglink.server.shoppingList.application

import com.felix.livinglink.server.core.domain.TimeProvider
import com.felix.livinglink.server.core.domain.UpdateOperationResult
import com.felix.livinglink.server.core.domain.UpdateResult
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItem
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItemRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.koin.core.annotation.Single
import kotlin.collections.plusAssign

@Single
class ChangeShoppingListItemsCompleteStateUseCase(
    private val shoppingListItemRepository: ShoppingListItemRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(input: Input): Output =
        coroutineScope {
            val results =
                input.idsToCompleteState
                    .map { (id, completeAction) ->
                        async {
                            val result =
                                shoppingListItemRepository.updateWithOptimisticLocking(id) { current ->
                                    when (completeAction) {
                                        true -> {
                                            if (current.isCompleted) {
                                                UpdateOperationResult.noUpdate(current = current)
                                            } else {
                                                val completed =
                                                    current.complete(
                                                        byUserId = input.byUserId,
                                                        at = timeProvider(),
                                                    )
                                                UpdateOperationResult.updated(newEntity = completed)
                                            }
                                        }
                                        false -> {
                                            if (!current.isCompleted) {
                                                UpdateOperationResult.noUpdate(current = current)
                                            } else {
                                                val unCompleted =
                                                    current.unComplete(
                                                        byUserId = input.byUserId,
                                                        at = timeProvider(),
                                                    )
                                                UpdateOperationResult.updated(newEntity = unCompleted)
                                            }
                                        }
                                    }
                                }

                            when (result) {
                                is UpdateResult.NotFound -> ItemResult.Missing(id)
                                is UpdateResult.OptimisticLockingError -> ItemResult.Conflict(id)
                                is UpdateResult.NotUpdated -> ItemResult.AlreadyChanged(result.response)
                                is UpdateResult.Updated -> ItemResult.Changed(result.newEntity)
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
        val idsToCompleteState: Map<String, Boolean>,
    )

    data class Output(
        val changedItems: List<ShoppingListItem>,
        val alreadyChangedItems: List<ShoppingListItem>,
        val missingIds: List<String>,
        val conflictedIds: List<String>,
    )
}
