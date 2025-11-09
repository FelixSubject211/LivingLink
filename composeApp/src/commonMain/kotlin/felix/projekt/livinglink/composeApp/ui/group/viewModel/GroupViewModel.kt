package felix.projekt.livinglink.composeApp.ui.group.viewModel

import felix.projekt.livinglink.composeApp.groups.interfaces.CreateInviteCodeUseCase
import felix.projekt.livinglink.composeApp.groups.interfaces.DeleteInviteCodeUseCase
import felix.projekt.livinglink.composeApp.groups.interfaces.GetGroupUseCase
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.ExecutionScope
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.MutableStateFlowWithReducer
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.Reducer
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.ViewModel
import felix.projekt.livinglink.composeApp.ui.group.viewModel.GroupResult.CloseDeleteInviteCodeConfirmation
import felix.projekt.livinglink.composeApp.ui.group.viewModel.GroupResult.DeleteInviteCodeLoading
import felix.projekt.livinglink.composeApp.ui.group.viewModel.GroupResult.GroupChangedToLoading
import felix.projekt.livinglink.composeApp.ui.group.viewModel.GroupResult.GroupsChanged
import felix.projekt.livinglink.composeApp.ui.group.viewModel.GroupResult.InviteCodeCreationCancelled
import felix.projekt.livinglink.composeApp.ui.group.viewModel.GroupResult.InviteCodeCreationNetworkError
import felix.projekt.livinglink.composeApp.ui.group.viewModel.GroupResult.InviteCodeCreationStarted
import felix.projekt.livinglink.composeApp.ui.group.viewModel.GroupResult.InviteCodeCreationSubmitting
import felix.projekt.livinglink.composeApp.ui.group.viewModel.GroupResult.InviteCodeCreationSucceeded
import felix.projekt.livinglink.composeApp.ui.group.viewModel.GroupResult.InviteCodeNameUpdated
import felix.projekt.livinglink.composeApp.ui.group.viewModel.GroupResult.ShowDeleteInviteCodeConfirmation
import felix.projekt.livinglink.composeApp.ui.group.viewModel.GroupSideEffect.CopyToClipboard.CopiedInviteCode
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow

class GroupViewModel(
    private val groupId: String,
    private val getGroupUseCase: GetGroupUseCase,
    private val createInviteCodeUseCase: CreateInviteCodeUseCase,
    private val deleteInviteCodeUseCase: DeleteInviteCodeUseCase,
    private val executionScope: ExecutionScope,
    private val reducer: Reducer<GroupState, GroupResult> = GroupReducer()
) : ViewModel<GroupState, GroupAction, GroupSideEffect> {
    private val _state = MutableStateFlowWithReducer(GroupState(), reducer)
    override val state: StateFlow<GroupState> = _state

    private val _sideEffect: MutableSharedFlow<GroupSideEffect> = MutableSharedFlow()
    override val sideEffect: MutableSharedFlow<GroupSideEffect> = _sideEffect

    override fun dispatch(action: GroupAction) = when (action) {
        is GroupAction.NavigateBack -> {
            executionScope.launchJob {
                _sideEffect.emit(GroupSideEffect.NavigateBack)
            }
        }

        is GroupAction.StartInviteCodeCreation -> {
            _state.update(InviteCodeCreationStarted)
        }

        is GroupAction.InviteCodeNameChanged -> {
            _state.update(InviteCodeNameUpdated(action.name))
        }

        is GroupAction.SubmitInviteCodeCreation -> {
            val createState = _state.value.inviteCodeCreation as GroupState.InviteCodeCreationState.Input
            executionScope.launchJob {
                createInviteCode(createState)
            }
        }

        is GroupAction.InviteCodeCreationCancelled -> {
            _state.update(InviteCodeCreationCancelled)
        }

        is GroupAction.CopyInviteCode -> {
            executionScope.launchJob {
                _sideEffect.emit(CopiedInviteCode(action.inviteCodeKey))
                _sideEffect.emit(GroupSideEffect.ShowSnackbar.InviteCodeCopied)
            }
        }

        is GroupAction.SubmitInviteCodeDeletion -> {
            _state.update(ShowDeleteInviteCodeConfirmation(action.inviteCodeId))
        }

        is GroupAction.ConfirmInviteCodeDeletion -> {
            val inviteCodeId = _state.value.inviteCodeIdToDeleted ?: return
            executionScope.launchJob {
                deleteInviteCode(inviteCodeId = inviteCodeId)
            }
        }

        is GroupAction.CancelInviteCodeDeletion -> {
            _state.update(CloseDeleteInviteCodeConfirmation)
        }
    }

    override fun start() {
        executionScope.launchCollector(getGroupUseCase(groupId)) { response ->
            when (response) {
                is GetGroupUseCase.Response.Loading -> {
                    _state.update(GroupChangedToLoading)
                }

                is GetGroupUseCase.Response.Data -> {
                    _state.update(
                        GroupsChanged(
                            groupId = response.group.id,
                            groupName = response.group.name,
                            memberIdToMemberName = response.group.memberIdToMemberName,
                            inviteCodes = response.group.inviteCodes.map { inviteCode ->
                                GroupState.InviteCode(
                                    id = inviteCode.id,
                                    name = inviteCode.name,
                                    creatorId = inviteCode.creatorId,
                                    usages = inviteCode.usages
                                )
                            }
                        )
                    )
                }
            }
        }
    }

    private suspend fun createInviteCode(createState: GroupState.InviteCodeCreationState.Input) {
        _state.update(InviteCodeCreationSubmitting)
        val response = createInviteCodeUseCase(
            groupId = groupId,
            inviteCodeName = createState.name
        )
        when (response) {
            is CreateInviteCodeUseCase.Response.Success -> {
                _state.update(
                    InviteCodeCreationSucceeded(
                        key = response.key
                    )
                )
            }

            is CreateInviteCodeUseCase.Response.NetworkError -> {
                _state.update(InviteCodeCreationNetworkError)
            }
        }
    }

    private suspend fun deleteInviteCode(inviteCodeId: String) {
        _state.update(DeleteInviteCodeLoading)
        val response = deleteInviteCodeUseCase(
            groupId = groupId,
            inviteCodeId = inviteCodeId
        )
        when (response) {
            is DeleteInviteCodeUseCase.Response.Success -> {
                _state.update(GroupResult.DeleteInviteCodeSuccess)
            }

            DeleteInviteCodeUseCase.Response.NetworkError -> {
                _state.update(GroupResult.DeleteInviteCodeError)
                _sideEffect.emit(GroupSideEffect.ShowSnackbar.DeleteInviteCodeNetworkError)
            }
        }
    }
}