package com.felix.livinglink.composeapp.shoppingList.application

import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListRepository
import org.koin.core.annotation.Single

interface SetShoppingListVisibleRangeUseCase {
    operator fun invoke(firstVisibleIndex: Int, lastVisibleIndex: Int)
}

@Single(binds = [SetShoppingListVisibleRangeUseCase::class])
class SetShoppingListVisibleRangeDefaultUseCase(
    private val shoppingListRepository: ShoppingListRepository,
) : SetShoppingListVisibleRangeUseCase {
    override fun invoke(firstVisibleIndex: Int, lastVisibleIndex: Int) =
        shoppingListRepository.setVisibleRange(firstVisibleIndex, lastVisibleIndex)
}