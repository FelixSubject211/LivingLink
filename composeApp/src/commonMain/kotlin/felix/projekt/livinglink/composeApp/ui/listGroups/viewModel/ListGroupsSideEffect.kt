package felix.projekt.livinglink.composeApp.ui.listGroups.viewModel

sealed class ListGroupsSideEffect {
    sealed class ShowSnackbar : ListGroupsSideEffect() {
        data object CreateGroupNetworkError : ShowSnackbar()
    }

    data object NavigateToSettings : ListGroupsSideEffect()
}