package felix.projekt.livinglink.composeApp.ui.listGroups.viewModel


sealed class ListGroupsResult {
    data object GroupsChangedToLoading : ListGroupsResult()
    data class GroupsChanged(val groups: List<ListGroupsState.Group>) : ListGroupsResult()
    data object MenuExpanded : ListGroupsResult()
    data object MenuClosed : ListGroupsResult()
    data object ShowAddGroupDialog : ListGroupsResult()
    data class AddGroupNameChanged(val value: String) : ListGroupsResult()
    data object CloseAddGroupDialog : ListGroupsResult()
    data object ConfirmAddGroup : ListGroupsResult()
    data object AddGroupFinished : ListGroupsResult()
    data object ShowJoinGroupDialog : ListGroupsResult()
    data class JoinGroupInviteCodeChanged(val value: String) : ListGroupsResult()
    data object CloseJoinGroupDialog : ListGroupsResult()
    data object ConfirmJoinGroup : ListGroupsResult()
    data object JoinGroupFinished : ListGroupsResult()
}