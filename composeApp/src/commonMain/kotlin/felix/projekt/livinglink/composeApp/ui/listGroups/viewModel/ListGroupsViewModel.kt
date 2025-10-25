package felix.projekt.livinglink.composeApp.ui.listGroups.viewModel

import felix.projekt.livinglink.composeApp.groups.interfaces.CreateGroupUseCase
import felix.projekt.livinglink.composeApp.groups.interfaces.GetGroupsUseCase
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.ExecutionScope
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.MutableStateFlowWithReducer
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.Reducer
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.ViewModel
import felix.projekt.livinglink.composeApp.ui.listGroups.viewModel.ListGroupsResult.AddGroupNameChanged
import felix.projekt.livinglink.composeApp.ui.listGroups.viewModel.ListGroupsResult.CloseAddGroupDialog
import felix.projekt.livinglink.composeApp.ui.listGroups.viewModel.ListGroupsResult.ShowAddGroupDialog
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow

class ListGroupsViewModel(
    private val getGroupsUseCase: GetGroupsUseCase,
    private val createGroupUseCase: CreateGroupUseCase,
    private val executionScope: ExecutionScope,
    private val reducer: Reducer<ListGroupsState, ListGroupsResult> = ListGroupsReducer(),
) : ViewModel<ListGroupsState, ListGroupsAction, ListGroupsSideEffect> {
    private val _state = MutableStateFlowWithReducer(ListGroupsState(), reducer)
    override val state: StateFlow<ListGroupsState> = _state

    private val _sideEffect: MutableSharedFlow<ListGroupsSideEffect> = MutableSharedFlow()
    override val sideEffect: MutableSharedFlow<ListGroupsSideEffect> = _sideEffect

    override fun dispatch(action: ListGroupsAction) = when (action) {
        ListGroupsAction.NavigateToSettings -> {
            executionScope.launchJob {
                _sideEffect.emit(ListGroupsSideEffect.NavigateToSettings)
            }
        }

        is ListGroupsAction.AddGroupSubmitted -> {
            _state.update(ShowAddGroupDialog)
        }

        is ListGroupsAction.AddGroupNameChanged -> {
            _state.update(AddGroupNameChanged(action.value))
        }

        is ListGroupsAction.AddGroupConfirmed -> {
            executionScope.launchJob {
                createGroup(_state.value)
            }
        }

        is ListGroupsAction.AddGroupCanceled -> {
            _state.update(CloseAddGroupDialog)
        }
    }

    fun start() {
        executionScope.launchCollector(getGroupsUseCase()) { response ->
            when (response) {
                GetGroupsUseCase.Response.Loading -> {
                    _state.update(ListGroupsResult.GroupsChangedToLoading)
                }

                is GetGroupsUseCase.Response.Data -> {
                    _state.update(
                        ListGroupsResult.GroupsChanged(
                            groups = response.groups.toListGroupsGroups()
                        )
                    )
                }
            }
        }
    }

    private fun List<GetGroupsUseCase.Group>.toListGroupsGroups() = this.map { group ->
        ListGroupsGroup(
            id = group.id,
            name = group.name,
            memberCount = group.memberCount
        )
    }

    private suspend fun createGroup(state: ListGroupsState) {
        _state.update(ListGroupsResult.ConfirmAddGroup)
        val response = createGroupUseCase(state.addGroupName)
        when (response) {
            CreateGroupUseCase.Response.Success -> {}
            CreateGroupUseCase.Response.NetworkError -> {
                _sideEffect.emit(ListGroupsSideEffect.ShowSnackbar.CreateGroupNetworkError)
            }
        }
        _state.update(ListGroupsResult.AddGroupFinished)
    }
}