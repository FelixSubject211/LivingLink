package com.felix.livinglink.composeapp.ui.shoppinglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felix.livinglink.composeapp.core.domain.Loadable
import com.felix.livinglink.composeapp.shoppingList.application.ChangeShoppingListItemCompleteStateUseCase
import com.felix.livinglink.composeapp.shoppingList.application.ObserveShoppingListUseCase
import com.felix.livinglink.composeapp.shoppingList.application.SetShoppingListVisibleRangeUseCase
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListContent
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class ShoppingListViewModel(
    private val setVisibleRangeUseCase: SetShoppingListVisibleRangeUseCase,
    private val changeItemCompleteStateUseCase: ChangeShoppingListItemCompleteStateUseCase,
    observeShoppingListUseCase: ObserveShoppingListUseCase,
) : ViewModel() {

    private val pendingItemIds = MutableStateFlow<Set<String>>(emptySet())

    val state: StateFlow<ShoppingListScreenState> =
        combine(
            observeShoppingListUseCase(),
            pendingItemIds,
        ) { loadable, pending ->
            loadable.toUiState(pending)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ShoppingListScreenState.initial,
        )

    private val _events = MutableSharedFlow<ShoppingListEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun onVisibleRangeChanged(firstVisibleIndex: Int, lastVisibleIndex: Int) {
        setVisibleRangeUseCase(firstVisibleIndex, lastVisibleIndex)
    }

    fun onToggleItem(itemId: String, completed: Boolean) {
        if (itemId in pendingItemIds.value) return

        viewModelScope.launch {
            pendingItemIds.update { it + itemId }
            try {
                val result = changeItemCompleteStateUseCase(itemId = itemId, completed = completed)
                if (result != ShoppingListRepository.ChangeCompleteStateResult.Success) {
                    _events.emit(ShoppingListEvent.ChangeFailed)
                }
            } finally {
                pendingItemIds.update { it - itemId }
            }
        }
    }

    private fun Loadable<ShoppingListContent>.toUiState(
        pendingItemIds: Set<String>,
    ): ShoppingListScreenState =
        when (this) {
            is Loadable.Loading -> ShoppingListScreenState.Loading
            is Loadable.Empty -> ShoppingListScreenState.Loading
            is Loadable.Error -> ShoppingListScreenState.Error
            is Loadable.Content ->
                ShoppingListScreenState.Content(
                    shoppingList = value,
                    pendingItemIds = pendingItemIds,
                )
        }
}

sealed interface ShoppingListEvent {
    data object ChangeFailed : ShoppingListEvent
}