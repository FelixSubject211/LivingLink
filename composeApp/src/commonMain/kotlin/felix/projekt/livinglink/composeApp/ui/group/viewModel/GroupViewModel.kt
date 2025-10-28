package felix.projekt.livinglink.composeApp.ui.group.viewModel

import felix.projekt.livinglink.composeApp.groups.interfaces.GetGroupUseCase
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.ExecutionScope
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.MutableStateFlowWithReducer
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.Reducer
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow

class GroupViewModel(
    private val groupId: String,
    private val getGroupUseCase: GetGroupUseCase,
    private val executionScope: ExecutionScope,
    private val reducer: Reducer<GroupState, GroupResult> = GroupReducer()
) : ViewModel<GroupState, GroupAction, GroupSideEffect> {
    private val _state = MutableStateFlowWithReducer(GroupState(), reducer)
    override val state: StateFlow<GroupState> = _state

    private val _sideEffect: MutableSharedFlow<GroupSideEffect> = MutableSharedFlow()
    override val sideEffect: MutableSharedFlow<GroupSideEffect> = _sideEffect

    override fun dispatch(action: GroupAction) = when (action) {
        GroupAction.NavigateBack -> {
            executionScope.launchJob {
                _sideEffect.emit(GroupSideEffect.NavigateBack)
            }
        }
    }

    fun start() {
        executionScope.launchCollector(getGroupUseCase(groupId)) { response ->
            when (response) {
                is GetGroupUseCase.Response.Loading -> {
                    _state.update(GroupResult.GroupChangedToLoading)
                }

                is GetGroupUseCase.Response.Data -> {
                    _state.update(
                        GroupResult.GroupsChanged(
                            groupId = response.group.id,
                            groupName = response.group.name
                        )
                    )
                }
            }
        }
    }
}