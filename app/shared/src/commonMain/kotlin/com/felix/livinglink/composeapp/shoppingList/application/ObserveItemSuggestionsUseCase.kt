package com.felix.livinglink.composeapp.shoppingList.application

import com.felix.livinglink.composeapp.shoppingList.domain.ItemSuggestion
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

interface ObserveItemSuggestionsUseCase {
    operator fun invoke(query: String): Flow<List<ItemSuggestion>>
}

@Single(binds = [ObserveItemSuggestionsUseCase::class])
class ObserveItemSuggestionsDefaultUseCase(
    private val shoppingListRepository: ShoppingListRepository,
) : ObserveItemSuggestionsUseCase {
    override fun invoke(query: String): Flow<List<ItemSuggestion>> =
        shoppingListRepository.observeSuggestions(query = query)
}