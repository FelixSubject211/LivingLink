package felix.projekt.livinglink.composeApp.shoppingList.interfaces

import felix.projekt.livinglink.composeApp.core.domain.PagingModel

interface GetShoppingListStateUseCase {
    operator fun invoke(groupId: String): PagingModel<ShoppingListItem>
    data class ShoppingListItem(
        val id: String,
        val name: String,
        val isChecked: Boolean
    )
}