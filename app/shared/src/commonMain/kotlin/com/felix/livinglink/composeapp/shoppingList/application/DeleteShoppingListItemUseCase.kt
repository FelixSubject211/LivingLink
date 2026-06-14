package com.felix.livinglink.composeapp.shoppingList.application

import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListRepository
import org.koin.core.annotation.Single

interface DeleteShoppingListItemUseCase {
    suspend operator fun invoke(itemId: String): ShoppingListRepository.DeleteResult
}

@Single(binds = [DeleteShoppingListItemUseCase::class])
class DeleteShoppingListItemDefaultUseCase(
    private val shoppingListRepository: ShoppingListRepository,
) : DeleteShoppingListItemUseCase {
    override suspend fun invoke(itemId: String): ShoppingListRepository.DeleteResult =
        shoppingListRepository.deleteItem(itemId = itemId)
}