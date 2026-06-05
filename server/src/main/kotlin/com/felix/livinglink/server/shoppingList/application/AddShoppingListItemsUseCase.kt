package com.felix.livinglink.server.shoppingList.application

import com.felix.livinglink.server.core.domain.TimeProvider
import com.felix.livinglink.server.core.domain.UuidGenerator
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItem
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItemRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.koin.core.annotation.Single

@Single
class AddShoppingListItemsUseCase(
    private val shoppingListItemRepository: ShoppingListItemRepository,
    private val uuidGenerator: UuidGenerator,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(input: Input): Output =
        coroutineScope {
            val items =
                input.names
                    .map { name ->
                        async {
                            val now = timeProvider()
                            shoppingListItemRepository.create(
                                ShoppingListItem(
                                    id = uuidGenerator(),
                                    name = name,
                                    createdByUserId = input.byUserId,
                                    completionEvents = emptyList(),
                                    createdAt = now,
                                    updatedAt = now,
                                ),
                            )
                        }
                    }.awaitAll()

            Output(items = items)
        }

    data class Input(
        val byUserId: String,
        val names: List<String>,
    )

    data class Output(
        val items: List<ShoppingListItem>,
    )
}
