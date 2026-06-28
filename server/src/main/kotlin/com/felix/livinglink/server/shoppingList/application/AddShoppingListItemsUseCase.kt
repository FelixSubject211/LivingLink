package com.felix.livinglink.server.shoppingList.application

import com.felix.livinglink.server.core.domain.OrderKeyProvider
import com.felix.livinglink.server.core.domain.TimeProvider
import com.felix.livinglink.server.core.domain.UuidGenerator
import com.felix.livinglink.server.group.application.RequireGroupMembershipUseCase
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItem
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItemRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.koin.core.annotation.Single

@Single
class AddShoppingListItemsUseCase(
    private val shoppingListItemRepository: ShoppingListItemRepository,
    private val requireGroupMembershipUseCase: RequireGroupMembershipUseCase,
    private val orderKeyProvider: OrderKeyProvider,
    private val uuidGenerator: UuidGenerator,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(input: Input): List<ShoppingListItem> {
        requireGroupMembershipUseCase(userId = input.byUserId, groupId = input.groupId)

        val last = shoppingListItemRepository.findLastPosition(input.groupId)
        val positions =
            orderKeyProvider
                .nKeysBetween(before = last, after = null, count = input.names.size)
                .map { orderKeyProvider.jitter(it) }

        val itemsToCreate =
            input.names.mapIndexed { index, name ->
                val now = timeProvider()
                ShoppingListItem(
                    id = uuidGenerator(),
                    groupId = input.groupId,
                    name = name,
                    createdByUserId = input.byUserId,
                    position = positions[index],
                    completionEvents = emptyList(),
                    createdAt = now,
                    updatedAt = now,
                )
            }

        return coroutineScope {
            itemsToCreate
                .map { item -> async { shoppingListItemRepository.create(item) } }
                .awaitAll()
        }
    }

    data class Input(
        val byUserId: String,
        val groupId: String,
        val names: List<String>,
    )
}
