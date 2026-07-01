package com.felix.livinglink.server.shoppingList.application

import com.felix.livinglink.server.group.application.RequireGroupMembershipUseCase
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItem
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItemRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.koin.core.annotation.Single
import kotlin.time.Instant

@Single
class AddShoppingListItemsUseCase(
    private val shoppingListItemRepository: ShoppingListItemRepository,
    private val requireGroupMembershipUseCase: RequireGroupMembershipUseCase,
) {
    suspend operator fun invoke(input: Input): List<ShoppingListItem> {
        requireGroupMembershipUseCase(userId = input.byUserId, groupId = input.groupId)

        val itemsToCreate =
            input.items.map { item ->
                ShoppingListItem(
                    id = item.id,
                    groupId = input.groupId,
                    name = item.name,
                    createdByUserId = input.byUserId,
                    position = item.position,
                    completionEvents = emptyList(),
                    createdAt = item.createdAt,
                    updatedAt = item.createdAt,
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
        val items: List<NewItem>,
    )

    data class NewItem(
        val id: String,
        val name: String,
        val position: String,
        val createdAt: Instant,
    )
}
