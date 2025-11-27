package felix.projekt.livinglink.composeApp.ui.shoppingListItemDetail.viewModel

import felix.projekt.livinglink.composeApp.shoppingList.domain.ShoppingListItemHistoryState
import felix.projekt.livinglink.composeApp.shoppingList.interfaces.DeleteShoppingListItemUseCase
import felix.projekt.livinglink.composeApp.shoppingList.interfaces.GetShoppingListItemHistoryUseCase
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.ExecutionScope
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.MutableStateFlowWithReducer
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.Reducer
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.ExperimentalTime

class ShoppingListItemDetailViewModel(
    private val groupId: String,
    private val itemId: String,
    private val getShoppingListItemHistoryUseCase: GetShoppingListItemHistoryUseCase,
    private val deleteShoppingListItemUseCase: DeleteShoppingListItemUseCase,
    private val executionScope: ExecutionScope,
    private val reducer: Reducer<ShoppingListItemDetailState, ShoppingListItemDetailResult> = ShoppingListItemDetailReducer()
) : ViewModel<ShoppingListItemDetailState, ShoppingListItemDetailAction, ShoppingListItemDetailSideEffect> {

    private val _state = MutableStateFlowWithReducer(ShoppingListItemDetailState(), reducer)
    override val state: StateFlow<ShoppingListItemDetailState> = _state

    private val _sideEffect: MutableSharedFlow<ShoppingListItemDetailSideEffect> = MutableSharedFlow()
    override val sideEffect: MutableSharedFlow<ShoppingListItemDetailSideEffect> = _sideEffect

    override fun dispatch(action: ShoppingListItemDetailAction) = when (action) {
        ShoppingListItemDetailAction.NavigateBack -> {
            executionScope.launchJob {
                _sideEffect.emit(ShoppingListItemDetailSideEffect.NavigateBack)
            }
        }

        ShoppingListItemDetailAction.DeleteItemClicked -> {
            _state.update(ShoppingListItemDetailResult.ShowDeleteConfirmation)
        }

        ShoppingListItemDetailAction.DeleteDialogDismissed -> {
            _state.update(ShoppingListItemDetailResult.HideDeleteConfirmation)
        }

        ShoppingListItemDetailAction.DeleteConfirmed -> {
            executionScope.launchJob { deleteItem() }
        }
    }

    @OptIn(ExperimentalTime::class)
    override fun start() {
        executionScope.launchCollector(
            getShoppingListItemHistoryUseCase(
                groupId = groupId,
                itemId = itemId
            )
        ) { response ->
            when (response) {
                is GetShoppingListItemHistoryUseCase.State.Loading -> {
                    _state.update(
                        ShoppingListItemDetailResult.Loading(
                            progress = response.progress
                        )
                    )
                }

                is GetShoppingListItemHistoryUseCase.State.Data -> {
                    val actions = response.actions.map { action ->
                        val actionType = when (action.actionType) {
                            ShoppingListItemHistoryState.ShoppingListItemHistoryActionType.Created -> {
                                ShoppingListItemDetailState.ActionType.Created
                            }

                            ShoppingListItemHistoryState.ShoppingListItemHistoryActionType.Checked -> {
                                ShoppingListItemDetailState.ActionType.Checked
                            }

                            ShoppingListItemHistoryState.ShoppingListItemHistoryActionType.Unchecked -> {
                                ShoppingListItemDetailState.ActionType.Unchecked
                            }

                            ShoppingListItemHistoryState.ShoppingListItemHistoryActionType.Deleted -> {
                                ShoppingListItemDetailState.ActionType.Deleted
                            }
                        }

                        ShoppingListItemDetailState.Action(
                            id = action.eventId,
                            userName = action.userName,
                            actionType = actionType,
                            createdAt = action.createdAt
                        )
                    }

                    _state.update(
                        ShoppingListItemDetailResult.DetailLoaded(
                            itemName = response.itemName,
                            actions = actions
                        )
                    )
                }
            }
        }
    }

    private suspend fun deleteItem() {
        _state.update(ShoppingListItemDetailResult.Deleting)
        val response = deleteShoppingListItemUseCase(
            groupId = groupId,
            itemId = itemId
        )
        when (response) {
            DeleteShoppingListItemUseCase.Response.Success -> {
                _sideEffect.emit(ShoppingListItemDetailSideEffect.NavigateBack)
            }

            DeleteShoppingListItemUseCase.Response.ItemNotFound -> {
                _sideEffect.emit(ShoppingListItemDetailSideEffect.ShowSnackbar.ItemNotFound)
            }

            DeleteShoppingListItemUseCase.Response.NetworkError -> {
                _sideEffect.emit(ShoppingListItemDetailSideEffect.ShowSnackbar.NetworkError)
            }
        }
        _state.update(ShoppingListItemDetailResult.DeleteFinished)
    }
}
