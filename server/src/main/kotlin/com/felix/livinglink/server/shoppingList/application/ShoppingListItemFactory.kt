package com.felix.livinglink.server.shoppingList.application

import com.felix.livinglink.server.core.domain.OrderKeyProvider
import com.felix.livinglink.server.core.domain.TimeProvider
import com.felix.livinglink.server.core.domain.UuidGenerator
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItemRepository
import org.koin.core.annotation.Single

@Single
class ShoppingListItemFactory(
    private val shoppingListItemRepository: ShoppingListItemRepository,
    private val orderKeyProvider: OrderKeyProvider,
    private val uuidGenerator: UuidGenerator,
    private val timeProvider: TimeProvider,
) {
    suspend fun createNewItems(
        groupId: String,
        names: List<String>,
    ): List<AddShoppingListItemsUseCase.NewItem> {
        val last = shoppingListItemRepository.findLastPosition(groupId)
        val positions =
            orderKeyProvider
                .nKeysBetween(before = last, after = null, count = names.size)
                .map { orderKeyProvider.jitter(it) }

        return names.mapIndexed { index, name ->
            AddShoppingListItemsUseCase.NewItem(
                id = uuidGenerator(),
                name = name,
                position = positions[index],
                createdAt = timeProvider(),
            )
        }
    }
}
