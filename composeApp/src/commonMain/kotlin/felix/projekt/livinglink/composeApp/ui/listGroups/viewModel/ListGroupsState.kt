package felix.projekt.livinglink.composeApp.ui.listGroups.viewModel

data class ListGroupsState(
    val groups: List<Group> = emptyList(),
    val groupsLoading: Boolean = true,
    val showAddGroup: Boolean = false,
    val addGroupName: String = "",
    val addGroupIsOngoing: Boolean = false
) {
    fun addGroupConfirmButtonIsEnabled(): Boolean {
        return !addGroupName.isBlank() && !addGroupIsOngoing
    }

    data class Group(
        val id: String,
        val name: String,
        val memberCount: Int
    )
}