package felix.projekt.livinglink.composeApp.ui.group.view

import GroupLocalizables
import felix.projekt.livinglink.composeApp.ui.group.viewModel.GroupSideEffect
import felix.projekt.livinglink.composeApp.ui.group.viewModel.GroupState

fun GroupState.InviteCodeCreationState.Input.Error.localized() = when (this) {
    GroupState.InviteCodeCreationState.Input.Error.NetworkError -> {
        GroupLocalizables.CreateInviteCodeNetworkError()
    }
}

fun GroupSideEffect.ShowSnackbar.localized() = when (this) {
    GroupSideEffect.ShowSnackbar.InviteCodeCopied -> {
        GroupLocalizables.InviteCodeCopiedSnackbar()
    }

    GroupSideEffect.ShowSnackbar.DeleteInviteCodeNetworkError -> {
        GroupLocalizables.DeleteInviteCodeNetworkError()
    }
}