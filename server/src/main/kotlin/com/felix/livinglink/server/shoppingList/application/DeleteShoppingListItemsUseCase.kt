package com.felix.livinglink.server.shoppingList.application

import com.felix.livinglink.server.core.domain.DeleteResult
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItemRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.koin.core.annotation.Single

@Single
class DeleteShoppingListItemsUseCase(
    private val shoppingListItemRepository: ShoppingListItemRepository,
) {
    suspend operator fun invoke(idsToDelete: Set<String>): Output =
        coroutineScope {
            val results =
                idsToDelete
                    .map { id ->
                        async {
                            id to shoppingListItemRepository.deleteById(id)
                        }
                    }.awaitAll()

            val (deletedPairs, missingPairs) =
                results.partition { (_, result) ->
                    result is DeleteResult.Deleted
                }

            Output(
                deletedIds = deletedPairs.map { it.first },
                missingIds = missingPairs.map { it.first },
            )
        }

    data class Output(
        val deletedIds: List<String>,
        val missingIds: List<String>,
    )
}
