package com.felix.livinglink.composeapp.ui.shoppinglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felix.livinglink.composeapp.core.domain.Loadable
import com.felix.livinglink.composeapp.shoppingList.application.ChangeShoppingListItemCompleteStateUseCase
import com.felix.livinglink.composeapp.shoppingList.application.DeleteShoppingListItemUseCase
import com.felix.livinglink.composeapp.shoppingList.application.ObserveShoppingListUseCase
import com.felix.livinglink.composeapp.shoppingList.application.SetShoppingListVisibleRangeUseCase
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListContent
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListItem
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
    private val deleteItemUseCase: DeleteShoppingListItemUseCase,
    observeShoppingListUseCase: ObserveShoppingListUseCase,
) : ViewModel() {

    private val pendingItemIds = MutableStateFlow<Set<String>>(emptySet())
    private val itemPendingDelete = MutableStateFlow<ShoppingListItem?>(null)

    val state: StateFlow<ShoppingListScreenState> =
        combine(
            observeShoppingListUseCase(),
            pendingItemIds,
            itemPendingDelete,
        ) { loadable, pending, pendingDelete ->
            loadable.toUiState(pending, pendingDelete)
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

    fun onRequestDeleteItem(item: ShoppingListItem) {
        itemPendingDelete.value = item
    }

    fun onCancelDelete() {
        itemPendingDelete.value = null
    }

    fun onConfirmDelete() {
        val item = itemPendingDelete.value ?: return
        itemPendingDelete.value = null

        if (item.id in pendingItemIds.value) return

        viewModelScope.launch {
            pendingItemIds.update { it + item.id }
            try {
                val result = deleteItemUseCase(itemId = item.id)
                if (result != ShoppingListRepository.DeleteResult.Success) {
                    _events.emit(ShoppingListEvent.DeleteFailed)
                }
            } finally {
                pendingItemIds.update { it - item.id }
            }
        }
    }

    private fun Loadable<ShoppingListContent>.toUiState(
        pendingItemIds: Set<String>,
        itemPendingDelete: ShoppingListItem?,
    ): ShoppingListScreenState =
        when (this) {
            is Loadable.Loading -> ShoppingListScreenState.Loading
            is Loadable.Empty -> ShoppingListScreenState.Empty
            is Loadable.Error -> ShoppingListScreenState.Error
            is Loadable.Content ->
                ShoppingListScreenState.Content(
                    shoppingList = value,
                    pendingItemIds = pendingItemIds,
                    itemPendingDelete = itemPendingDelete,
                )
        }
}

sealed interface ShoppingListEvent {
    data object ChangeFailed : ShoppingListEvent
    data object DeleteFailed : ShoppingListEvent
}