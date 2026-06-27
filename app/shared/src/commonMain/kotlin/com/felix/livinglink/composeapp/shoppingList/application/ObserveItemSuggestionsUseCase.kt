package com.felix.livinglink.composeapp.shoppingList.application

import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListItemSuggestion
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

interface ObserveItemSuggestionsUseCase {
    operator fun invoke(query: String): Flow<List<ShoppingListItemSuggestion>>
}

@Single(binds = [ObserveItemSuggestionsUseCase::class])
class ObserveItemSuggestionsDefaultUseCase(
    private val shoppingListRepository: ShoppingListRepository,
) : ObserveItemSuggestionsUseCase {
    override fun invoke(query: String): Flow<List<ShoppingListItemSuggestion>> =
        shoppingListRepository.observeSuggestions(query = query)
}