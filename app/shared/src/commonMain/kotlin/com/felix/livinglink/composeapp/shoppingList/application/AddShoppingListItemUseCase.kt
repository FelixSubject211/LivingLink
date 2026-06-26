package com.felix.livinglink.composeapp.shoppingList.application

import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListRepository
import org.koin.core.annotation.Single

interface AddShoppingListItemUseCase {
    suspend operator fun invoke(name: String): Result

    enum class Result {
        Success,
        NetworkError,
        NoActiveGroup,
    }
}

@Single(binds = [AddShoppingListItemUseCase::class])
class AddShoppingListItemDefaultUseCase(
    private val shoppingListRepository: ShoppingListRepository,
) : AddShoppingListItemUseCase {
    override suspend fun invoke(name: String): AddShoppingListItemUseCase.Result =
        when (shoppingListRepository.addItem(name)) {
            ShoppingListRepository.AddResult.Success ->
                AddShoppingListItemUseCase.Result.Success
            ShoppingListRepository.AddResult.NetworkError ->
                AddShoppingListItemUseCase.Result.NetworkError
            ShoppingListRepository.AddResult.NoActiveGroup ->
                AddShoppingListItemUseCase.Result.NoActiveGroup
        }
}