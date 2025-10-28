package felix.projekt.livinglink.composeApp.ui.group.viewModel

sealed class GroupResult {
    data object GroupChangedToLoading : GroupResult()
    data class GroupsChanged(val groupId: String, val groupName: String) : GroupResult()
}