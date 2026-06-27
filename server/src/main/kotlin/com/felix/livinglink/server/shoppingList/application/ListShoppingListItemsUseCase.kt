package com.felix.livinglink.server.shoppingList.application

import com.felix.livinglink.server.group.application.RequireGroupMembershipUseCase
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItem
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItemQuery
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItemRepository
import org.koin.core.annotation.Single

@Single
class ListShoppingListItemsUseCase(
    private val shoppingListItemRepository: ShoppingListItemRepository,
    private val requireGroupMembershipUseCase: RequireGroupMembershipUseCase,
) {
    suspend operator fun invoke(input: Input): List<ShoppingListItem> {
        requireGroupMembershipUseCase(userId = input.byUserId, groupId = input.groupId)

        return shoppingListItemRepository.find(
            ShoppingListItemQuery(
                groupId = input.groupId,
                completed = input.completed,
                limit = input.limit,
                offset = 0,
            ),
        )
    }

    data class Input(
        val byUserId: String,
        val groupId: String,
        val completed: Boolean?,
        val limit: Int,
    )
}
