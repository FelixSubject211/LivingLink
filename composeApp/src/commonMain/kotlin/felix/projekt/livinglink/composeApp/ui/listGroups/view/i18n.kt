package felix.projekt.livinglink.composeApp.ui.listGroups.view

import ListGroupsLocalizables
import felix.projekt.livinglink.composeApp.ui.listGroups.viewModel.ListGroupsSideEffect

fun ListGroupsSideEffect.ShowSnackbar.localized() = when (this) {
    ListGroupsSideEffect.ShowSnackbar.CreateGroupNetworkError -> {
        ListGroupsLocalizables.CreateGroupNetworkError()
    }

    ListGroupsSideEffect.ShowSnackbar.JoinGroupNetworkError -> {
        ListGroupsLocalizables.JoinGroupNetworkError()
    }

    ListGroupsSideEffect.ShowSnackbar.JoinGroupInvalidInviteCode -> {
        ListGroupsLocalizables.JoinGroupInvalidInviteCodeError()
    }

    ListGroupsSideEffect.ShowSnackbar.JoinGroupAlreadyMember -> {
        ListGroupsLocalizables.JoinGroupAlreadyMemberError()
    }
}