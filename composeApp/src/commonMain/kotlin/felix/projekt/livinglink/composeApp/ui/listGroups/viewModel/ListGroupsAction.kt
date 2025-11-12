package felix.projekt.livinglink.composeApp.ui.listGroups.viewModel

sealed class ListGroupsAction {
    data object NavigateToSettings : ListGroupsAction()
    data object ExpandMenu : ListGroupsAction()
    data object CloseMenu : ListGroupsAction()
    data object AddGroupSubmitted : ListGroupsAction()
    data class AddGroupNameChanged(val value: String) : ListGroupsAction()
    data object AddGroupConfirmed : ListGroupsAction()
    data object AddGroupCanceled : ListGroupsAction()
    data object JoinGroupSubmitted : ListGroupsAction()
    data class JoinGroupInviteCodeChanged(val value: String) : ListGroupsAction()
    data object JoinGroupConfirmed : ListGroupsAction()
    data object JoinGroupCanceled : ListGroupsAction()
    data class NavigateToGroup(val groupId: String) : ListGroupsAction()
}