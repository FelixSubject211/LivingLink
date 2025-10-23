package felix.projekt.livinglink.composeApp.ui.listGroups.view

import ListGroupsLocalizables
import felix.projekt.livinglink.composeApp.ui.listGroups.viewModel.ListGroupsSideEffect

fun ListGroupsSideEffect.ShowSnackbar.localized() = when (this) {
    ListGroupsSideEffect.ShowSnackbar.CreateGroupNetworkError -> {
        ListGroupsLocalizables.CreateGroupNetworkError()
    }
}