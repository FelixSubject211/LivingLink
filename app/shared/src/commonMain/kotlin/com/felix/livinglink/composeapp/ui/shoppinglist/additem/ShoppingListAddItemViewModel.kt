package com.felix.livinglink.composeapp.ui.shoppinglist.additem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felix.livinglink.composeapp.shoppingList.application.AddShoppingListItemUseCase
import com.felix.livinglink.composeapp.shoppingList.application.ObserveItemSuggestionsUseCase
import com.felix.livinglink.composeapp.shoppingList.domain.ItemSuggestion
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@OptIn(ExperimentalCoroutinesApi::class)
@KoinViewModel
class ShoppingListAddItemViewModel(
    private val addShoppingListItemUseCase: AddShoppingListItemUseCase,
    observeItemSuggestionsUseCase: ObserveItemSuggestionsUseCase,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val isAdding = MutableStateFlow(false)

    private val suggestions: Flow<List<ItemSuggestion>> =
        query.flatMapLatest { current ->
            observeItemSuggestionsUseCase(query = current)
        }

    val state: StateFlow<ShoppingListAddItemState> =
        combine(
            query,
            suggestions,
            isAdding,
        ) { currentQuery, currentSuggestions, adding ->
            ShoppingListAddItemState(
                query = currentQuery,
                suggestions = currentSuggestions,
                isAdding = adding,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ShoppingListAddItemState.initial,
        )

    private val _events = MutableSharedFlow<AddItemEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun onQueryChanged(value: String) {
        query.value = value
    }

    fun onSubmit() {
        val name = query.value.trim()
        if (name.isBlank() || isAdding.value) return
        addItem(name)
    }

    fun onSuggestionSelected(suggestion: ItemSuggestion) {
        if (isAdding.value) return
        addItem(suggestion.name)
    }

    private fun addItem(name: String) {
        viewModelScope.launch {
            isAdding.update { true }
            try {
                val result = addShoppingListItemUseCase(name)
                if (result == AddShoppingListItemUseCase.Result.Success) {
                    query.value = ""
                    _events.emit(AddItemEvent.Added)
                } else {
                    _events.emit(AddItemEvent.AddFailed)
                }
            } finally {
                isAdding.update { false }
            }
        }
    }
}