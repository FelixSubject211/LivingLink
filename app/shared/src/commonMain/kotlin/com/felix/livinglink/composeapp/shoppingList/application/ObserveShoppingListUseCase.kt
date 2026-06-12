package com.felix.livinglink.composeapp.shoppingList.application

import com.felix.livinglink.composeapp.core.domain.Loadable
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListContent
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

interface ObserveShoppingListUseCase {
    operator fun invoke(): Flow<Loadable<ShoppingListContent>>
}

@Single(binds = [ObserveShoppingListUseCase::class])
class ObserveShoppingListDefaultUseCase(
    private val shoppingListRepository: ShoppingListRepository,
) : ObserveShoppingListUseCase {
    override fun invoke(): Flow<Loadable<ShoppingListContent>> = shoppingListRepository.state
}