package com.felix.livinglink.server.shoppingList.application

import com.felix.livinglink.server.core.domain.OrderKeyProvider
import com.felix.livinglink.server.core.domain.TimeProvider
import com.felix.livinglink.server.core.domain.UpdateOperationResult
import com.felix.livinglink.server.core.domain.UpdateResult
import com.felix.livinglink.server.group.application.RequireGroupMembershipUseCase
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItem
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItemRepository
import org.koin.core.annotation.Single

@Single
class MoveShoppingListItemsUseCase(
    private val shoppingListItemRepository: ShoppingListItemRepository,
    private val requireGroupMembershipUseCase: RequireGroupMembershipUseCase,
    private val orderKeyProvider: OrderKeyProvider,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(input: Input): Output {
        requireGroupMembershipUseCase(userId = input.byUserId, groupId = input.groupId)

        val movedItems = mutableListOf<ShoppingListItem>()
        val missingIds = mutableListOf<String>()
        val anchorNotFoundIds = mutableListOf<String>()
        val conflictedIds = mutableListOf<String>()

        for (move in input.moves) {
            when (val result = applySingle(input.groupId, move)) {
                is SingleResult.Moved -> movedItems += result.item
                is SingleResult.Missing -> missingIds += result.id
                is SingleResult.AnchorMissing -> anchorNotFoundIds += result.id
                is SingleResult.Conflict -> conflictedIds += result.id
            }
        }

        return Output(
            movedItems = movedItems,
            missingIds = missingIds,
            anchorNotFoundIds = anchorNotFoundIds,
            conflictedIds = conflictedIds,
        )
    }

    private suspend fun applySingle(groupId: String, move: Move): SingleResult {
        val item =
            shoppingListItemRepository
                .findById(move.itemId)
                ?.takeIf { it.groupId == groupId }
                ?: return SingleResult.Missing(move.itemId)

        val anchorId =
            when (move) {
                is Move.After -> move.afterId
                is Move.Before -> move.beforeId
            }

        if (anchorId == move.itemId) {
            return SingleResult.Moved(item)
        }

        val anchor =
            shoppingListItemRepository
                .findById(anchorId)
                ?.takeIf { it.groupId == groupId }
                ?: return SingleResult.AnchorMissing(anchorId)

        val (lowerKey, upperKey) =
            when (move) {
                is Move.After ->
                    shoppingListItemRepository.findPositionBelow(
                        groupId = groupId,
                        position = anchor.position,
                        excludingIds = setOf(move.itemId),
                    ) to anchor.position

                is Move.Before ->
                    anchor.position to
                        shoppingListItemRepository.findPositionAbove(
                            groupId = groupId,
                            position = anchor.position,
                            excludingIds = setOf(move.itemId),
                        )
            }

        val newPosition = orderKeyProvider.between(before = lowerKey, after = upperKey)
        if (newPosition == item.position) {
            return SingleResult.Moved(item)
        }

        val result =
            shoppingListItemRepository.updateWithOptimisticLocking(move.itemId) { current ->
                UpdateOperationResult.updated(
                    newEntity = current.moveTo(position = newPosition, at = timeProvider()),
                )
            }

        return when (result) {
            is UpdateResult.NotFound -> SingleResult.Missing(move.itemId)
            is UpdateResult.OptimisticLockingError -> SingleResult.Conflict(move.itemId)
            is UpdateResult.NotUpdated -> SingleResult.Moved(result.response)
            is UpdateResult.Updated -> SingleResult.Moved(result.newEntity)
        }
    }

    private sealed interface SingleResult {
        data class Moved(
            val item: ShoppingListItem,
        ) : SingleResult

        data class Missing(
            val id: String,
        ) : SingleResult

        data class AnchorMissing(
            val id: String,
        ) : SingleResult

        data class Conflict(
            val id: String,
        ) : SingleResult
    }

    sealed interface Move {
        val itemId: String

        data class After(
            override val itemId: String,
            val afterId: String,
        ) : Move

        data class Before(
            override val itemId: String,
            val beforeId: String,
        ) : Move
    }

    data class Input(
        val byUserId: String,
        val groupId: String,
        val moves: List<Move>,
    )

    data class Output(
        val movedItems: List<ShoppingListItem>,
        val missingIds: List<String>,
        val anchorNotFoundIds: List<String>,
        val conflictedIds: List<String>,
    )
}
