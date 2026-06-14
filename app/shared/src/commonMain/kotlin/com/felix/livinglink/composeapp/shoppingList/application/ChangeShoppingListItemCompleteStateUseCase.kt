package com.felix.livinglink.composeapp.shoppingList.application

import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListRepository
import org.koin.core.annotation.Single

interface ChangeShoppingListItemCompleteStateUseCase {
    suspend operator fun invoke(itemId: String, completed: Boolean): ShoppingListRepository.ChangeCompleteStateResult
}

@Single(binds = [ChangeShoppingListItemCompleteStateUseCase::class])
class ChangeShoppingListItemCompleteStateDefaultUseCase(
    private val shoppingListRepository: ShoppingListRepository,
) : ChangeShoppingListItemCompleteStateUseCase {
    override suspend fun invoke(itemId: String, completed: Boolean): ShoppingListRepository.ChangeCompleteStateResult =
        shoppingListRepository.changeItemCompleteState(itemId = itemId, completed = completed)
}