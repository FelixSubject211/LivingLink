package felix.projekt.livinglink.composeApp.ui.listGroups.viewModel

sealed class ListGroupsSideEffect {
    sealed class ShowSnackbar : ListGroupsSideEffect() {
        data object CreateGroupNetworkError : ShowSnackbar()
        data object JoinGroupNetworkError : ShowSnackbar()
        data object JoinGroupInvalidInviteCode : ShowSnackbar()
        data object JoinGroupAlreadyMember : ShowSnackbar()
    }

    data object NavigateToSettings : ListGroupsSideEffect()

    data class NavigateToGroup(val groupId: String) : ListGroupsSideEffect()
}