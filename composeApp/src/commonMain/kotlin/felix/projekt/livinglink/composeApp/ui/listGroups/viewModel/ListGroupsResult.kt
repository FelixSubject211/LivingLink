package felix.projekt.livinglink.composeApp.ui.listGroups.viewModel


sealed class ListGroupsResult {
    data object GroupsChangedToLoading : ListGroupsResult()
    data class GroupsChanged(val groups: List<ListGroupsGroup>) : ListGroupsResult()
    data object ShowAddGroupDialog : ListGroupsResult()
    data class AddGroupNameChanged(val value: String) : ListGroupsResult()
    data object CloseAddGroupDialog : ListGroupsResult()
    data object ConfirmAddGroup : ListGroupsResult()
    data object AddGroupFinished : ListGroupsResult()
}