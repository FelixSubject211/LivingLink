package felix.projekt.livinglink.composeApp.ui.shoppingList.viewModel

import felix.projekt.livinglink.composeApp.shoppingList.interfaces.CheckShoppingListItemUseCase
import felix.projekt.livinglink.composeApp.shoppingList.interfaces.CreateShoppingListItemUseCase
import felix.projekt.livinglink.composeApp.shoppingList.interfaces.GetShoppingListStateUseCase
import felix.projekt.livinglink.composeApp.shoppingList.interfaces.UncheckShoppingListItemUseCase
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.ExecutionScope
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.MutableStateFlowWithReducer
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.Reducer
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.ViewModel
import felix.projekt.livinglink.composeApp.ui.shoppingList.viewModel.ShoppingListResult.AddItemFinished
import felix.projekt.livinglink.composeApp.ui.shoppingList.viewModel.ShoppingListResult.AddItemSubmitting
import felix.projekt.livinglink.composeApp.ui.shoppingList.viewModel.ShoppingListResult.NewItemNameUpdated
import felix.projekt.livinglink.composeApp.ui.shoppingList.viewModel.ShoppingListResult.ShoppingListChanged
import felix.projekt.livinglink.composeApp.ui.shoppingList.viewModel.ShoppingListResult.ShoppingListLoading
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow

class ShoppingListViewModel(
    private val groupId: String,
    private val getShoppingListStateUseCase: GetShoppingListStateUseCase,
    private val createShoppingListItemUseCase: CreateShoppingListItemUseCase,
    private val checkShoppingListItemUseCase: CheckShoppingListItemUseCase,
    private val uncheckShoppingListItemUseCase: UncheckShoppingListItemUseCase,
    private val executionScope: ExecutionScope,
    private val reducer: Reducer<ShoppingListState, ShoppingListResult> = ShoppingListReducer()
) : ViewModel<ShoppingListState, ShoppingListAction, ShoppingListSideEffect> {

    private val _state = MutableStateFlowWithReducer(ShoppingListState(), reducer)
    override val state: StateFlow<ShoppingListState> = _state

    private val _sideEffect: MutableSharedFlow<ShoppingListSideEffect> = MutableSharedFlow()
    override val sideEffect: MutableSharedFlow<ShoppingListSideEffect> = _sideEffect

    override fun dispatch(action: ShoppingListAction) {
        when (action) {
            is ShoppingListAction.NavigateBack -> {
                executionScope.launchJob {
                    _sideEffect.emit(ShoppingListSideEffect.NavigateBack)
                }
            }

            is ShoppingListAction.NewItemNameChanged -> {
                _state.update(NewItemNameUpdated(action.name))
            }

            is ShoppingListAction.SubmitNewItem -> {
                val name = _state.value.newItemName.trim()
                executionScope.launchJob {
                    createItem(name)
                }
            }

            is ShoppingListAction.ItemChecked -> {
                executionScope.launchJob {
                    itemChecked(action.itemId)
                }
            }

            is ShoppingListAction.ItemUnchecked -> {
                executionScope.launchJob {
                    itemUnchecked(action.itemId)
                }
            }

            is ShoppingListAction.OpenItemDetail -> {
                executionScope.launchJob {
                    _sideEffect.emit(
                        ShoppingListSideEffect.NavigateToItemDetail(
                            itemId = action.itemId,
                            itemName = action.itemName
                        )
                    )
                }
            }
        }
    }

    override fun start() {
        executionScope.launchCollector(getShoppingListStateUseCase(groupId)) { response ->
            when (response) {
                is GetShoppingListStateUseCase.State.Loading -> {
                    _state.update(
                        ShoppingListLoading(progress = response.progress)
                    )
                }

                is GetShoppingListStateUseCase.State.Data -> {
                    _state.update(
                        ShoppingListChanged(
                            items = response.items.map { item ->
                                ShoppingListState.Item(
                                    id = item.id,
                                    name = item.name,
                                    isChecked = item.isChecked
                                )
                            }
                        )
                    )
                }
            }
        }
    }

    private suspend fun createItem(name: String) {
        _state.update(AddItemSubmitting)
        val response = createShoppingListItemUseCase(
            groupId = groupId,
            name = name
        )
        when (response) {
            is CreateShoppingListItemUseCase.Response.Success -> {
                _state.update(AddItemFinished)
            }

            is CreateShoppingListItemUseCase.Response.NetworkError -> {
                _sideEffect.emit(ShoppingListSideEffect.ShowSnackbar.CreateItemNetworkError)
            }
        }
    }

    private suspend fun itemChecked(itemId: String) {
        _state.update(ShoppingListResult.ItemCheckedSubmitting(itemId = itemId))
        val response = checkShoppingListItemUseCase(
            groupId = groupId,
            itemId = itemId
        )
        when (response) {
            is CheckShoppingListItemUseCase.Response.Success -> {}

            is CheckShoppingListItemUseCase.Response.AlreadyChecked -> {
                _sideEffect.emit(ShoppingListSideEffect.ShowSnackbar.ItemCheckedAlreadyChecked)
            }

            is CheckShoppingListItemUseCase.Response.ItemNotFound -> {
                _sideEffect.emit(ShoppingListSideEffect.ShowSnackbar.ItemCheckedNotFound)
            }

            is CheckShoppingListItemUseCase.Response.NetworkError -> {
                _sideEffect.emit(ShoppingListSideEffect.ShowSnackbar.ItemCheckedNetworkError)
            }
        }
        _state.update(ShoppingListResult.ItemCheckedFinished(itemId = itemId))
    }

    private suspend fun itemUnchecked(itemId: String) {
        _state.update(ShoppingListResult.ItemUncheckedSubmitting(itemId = itemId))
        val response = uncheckShoppingListItemUseCase(
            groupId = groupId,
            itemId = itemId
        )
        when (response) {
            is UncheckShoppingListItemUseCase.Response.Success -> {}

            is UncheckShoppingListItemUseCase.Response.AlreadyUnchecked -> {
                _sideEffect.emit(ShoppingListSideEffect.ShowSnackbar.ItemUncheckedAlreadyUnchecked)
            }

            is UncheckShoppingListItemUseCase.Response.ItemNotFound -> {
                _sideEffect.emit(ShoppingListSideEffect.ShowSnackbar.ItemUncheckedNotFound)
            }

            is UncheckShoppingListItemUseCase.Response.NetworkError -> {
                _sideEffect.emit(ShoppingListSideEffect.ShowSnackbar.ItemUncheckedNetworkError)
            }
        }
        _state.update(ShoppingListResult.ItemUncheckedFinished(itemId = itemId))
    }
}