package felix.projekt.livinglink.composeApp.ui.group.viewModel

import felix.projekt.livinglink.composeApp.ui.core.viewmodel.Reducer
import felix.projekt.livinglink.composeApp.ui.group.viewModel.GroupState.InviteCodeCreationState.Input
import felix.projekt.livinglink.composeApp.ui.group.viewModel.GroupState.InviteCodeCreationState.None
import felix.projekt.livinglink.composeApp.ui.group.viewModel.GroupState.InviteCodeCreationState.Success

class GroupReducer : Reducer<GroupState, GroupResult> {
    override fun invoke(
        state: GroupState,
        result: GroupResult
    ) = when (result) {
        is GroupResult.GroupChangedToLoading -> {
            state.copy(
                groupId = null,
                groupName = null,
                memberIdToMemberName = emptyMap(),
                inviteCodes = emptyList(),
                groupIsLoading = true
            )
        }

        is GroupResult.GroupsChanged -> {
            state.copy(
                groupId = result.groupId,
                groupName = result.groupName,
                memberIdToMemberName = result.memberIdToMemberName,
                inviteCodes = result.inviteCodes,
                groupIsLoading = false
            )
        }

        is GroupResult.InviteCodeCreationStarted -> {
            state.copy(
                inviteCodeCreation = Input(
                    name = "",
                    isLoading = false,
                    error = null
                )
            )
        }

        is GroupResult.InviteCodeNameUpdated -> {
            val creationState = state.inviteCodeCreation as? Input ?: return state
            state.copy(
                inviteCodeCreation = creationState.copy(
                    name = result.name
                )
            )
        }

        is GroupResult.InviteCodeCreationSubmitting -> {
            val creationState = state.inviteCodeCreation as? Input ?: return state
            state.copy(
                inviteCodeCreation = creationState.copy(
                    isLoading = true
                )
            )
        }

        is GroupResult.InviteCodeCreationCancelled -> {
            state.copy(
                inviteCodeCreation = None
            )
        }

        is GroupResult.InviteCodeCreationSucceeded -> {
            state.copy(
                inviteCodeCreation = Success(
                    key = result.key
                )
            )
        }

        GroupResult.InviteCodeCreationNetworkError -> {
            val creationState = state.inviteCodeCreation as? Input ?: return state
            state.copy(
                inviteCodeCreation = creationState.copy(
                    isLoading = false,
                    error = Input.Error.NetworkError
                )
            )
        }

        is GroupResult.ShowDeleteInviteCodeConfirmation -> {
            state.copy(
                showDeleteInviteCodeConfirmation = true,
                inviteCodeIdToDeleted = result.inviteCodeId,
                deleteInviteCodeIsLoading = false
            )
        }

        is GroupResult.CloseDeleteInviteCodeConfirmation -> {
            state.copy(
                showDeleteInviteCodeConfirmation = false,
                inviteCodeIdToDeleted = null,
                deleteInviteCodeIsLoading = false
            )
        }

        is GroupResult.DeleteInviteCodeLoading -> {
            state.copy(
                deleteInviteCodeIsLoading = true
            )
        }

        is GroupResult.DeleteInviteCodeSuccess -> {
            state.copy(
                showDeleteInviteCodeConfirmation = false,
                deleteInviteCodeIsLoading = false,
                inviteCodeIdToDeleted = null,
                inviteCodes = state.inviteCodes.filterNot {
                    it.id == state.inviteCodeIdToDeleted
                }
            )
        }

        is GroupResult.DeleteInviteCodeError -> {
            state.copy(
                showDeleteInviteCodeConfirmation = false,
                deleteInviteCodeIsLoading = false,
                inviteCodeIdToDeleted = null
            )
        }
    }
}