package com.felix.livinglink.composeapp.ui.shoppinglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felix.livinglink.composeapp.core.domain.Loadable
import com.felix.livinglink.composeapp.shoppingList.application.ObserveShoppingListUseCase
import com.felix.livinglink.composeapp.shoppingList.application.SetShoppingListVisibleRangeUseCase
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListContent
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class ShoppingListViewModel(
    private val setVisibleRangeUseCase: SetShoppingListVisibleRangeUseCase,
    observeShoppingListUseCase: ObserveShoppingListUseCase,
) : ViewModel() {

    val state: StateFlow<ShoppingListScreenState> =
        observeShoppingListUseCase()
            .map { it.toUiState() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ShoppingListScreenState.initial,
            )

    fun onVisibleRangeChanged(firstVisibleIndex: Int, lastVisibleIndex: Int) {
        setVisibleRangeUseCase(firstVisibleIndex, lastVisibleIndex)
    }

    private fun Loadable<ShoppingListContent>.toUiState(): ShoppingListScreenState =
        when (this) {
            is Loadable.Loading -> ShoppingListScreenState.Loading
            is Loadable.Empty -> ShoppingListScreenState.Loading
            is Loadable.Error -> ShoppingListScreenState.Error
            is Loadable.Content -> ShoppingListScreenState.Content(shoppingList = value)
        }
}