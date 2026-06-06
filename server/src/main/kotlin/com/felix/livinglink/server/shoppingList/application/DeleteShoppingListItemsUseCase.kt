package com.felix.livinglink.server.shoppingList.application

import com.felix.livinglink.server.core.domain.DeleteResult
import com.felix.livinglink.server.group.application.RequireGroupMembershipUseCase
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItemRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.koin.core.annotation.Single

@Single
class DeleteShoppingListItemsUseCase(
    private val shoppingListItemRepository: ShoppingListItemRepository,
    private val requireGroupMembershipUseCase: RequireGroupMembershipUseCase,
) {
    suspend operator fun invoke(input: Input): Output {
        requireGroupMembershipUseCase(userId = input.byUserId, groupId = input.groupId)

        return coroutineScope {
            val results =
                input.idsToDelete
                    .map { id ->
                        async {
                            val existing = shoppingListItemRepository.findById(id)
                            if (existing == null || existing.groupId != input.groupId) {
                                id to false
                            } else {
                                id to (shoppingListItemRepository.deleteById(id) is DeleteResult.Deleted)
                            }
                        }
                    }.awaitAll()

            val (deletedPairs, missingPairs) = results.partition { it.second }

            Output(
                deletedIds = deletedPairs.map { it.first },
                missingIds = missingPairs.map { it.first },
            )
        }
    }

    data class Input(
        val byUserId: String,
        val groupId: String,
        val idsToDelete: Set<String>,
    )

    data class Output(
        val deletedIds: List<String>,
        val missingIds: List<String>,
    )
}
