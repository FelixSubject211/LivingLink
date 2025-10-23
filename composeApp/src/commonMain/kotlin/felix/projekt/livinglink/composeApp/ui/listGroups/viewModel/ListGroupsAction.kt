package felix.projekt.livinglink.composeApp.ui.listGroups.viewModel

sealed class ListGroupsAction {
    data object AddGroupSubmitted : ListGroupsAction()
    data class AddGroupNameChanged(val value: String) : ListGroupsAction()
    data object AddGroupConfirmed : ListGroupsAction()
    data object AddGroupCanceled : ListGroupsAction()
}