package com.felix.livinglink.server.shoppingList.application

import com.felix.livinglink.server.group.application.RequireGroupMembershipUseCase
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItem
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItemQuery
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItemRepository
import org.koin.core.annotation.Single

@Single
class GetShoppingListItemsPageUseCase(
    private val shoppingListItemRepository: ShoppingListItemRepository,
    private val requireGroupMembershipUseCase: RequireGroupMembershipUseCase,
) {
    suspend operator fun invoke(input: Input): Output {
        require(input.limit >= 1) { "limit must be >= 1" }
        require(input.offset >= 0) { "offset must be >= 0" }

        requireGroupMembershipUseCase(userId = input.byUserId, groupId = input.groupId)

        val query =
            ShoppingListItemQuery(
                groupId = input.groupId,
                completed = input.completed,
                limit = input.limit + 1,
                offset = input.offset,
            )

        val fetched = shoppingListItemRepository.find(query)
        val totalCount = shoppingListItemRepository.count(query)

        val hasMore = fetched.size > input.limit
        val items = if (hasMore) fetched.take(input.limit) else fetched

        return Output(
            items = items,
            totalCount = totalCount,
        )
    }

    data class Input(
        val byUserId: String,
        val groupId: String,
        val completed: Boolean?,
        val limit: Int,
        val offset: Int,
    )

    data class Output(
        val items: List<ShoppingListItem>,
        val totalCount: Long,
    )
}
