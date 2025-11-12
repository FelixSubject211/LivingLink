package felix.projekt.livinglink.composeApp.ui.listGroups.viewModel

data class ListGroupsState(
    val groups: List<Group> = emptyList(),
    val groupsLoading: Boolean = true,
    val menuExpanded: Boolean = false,
    val showAddGroup: Boolean = false,
    val addGroupName: String = "",
    val addGroupIsLoading: Boolean = false,
    val showJoinGroup: Boolean = false,
    val joinGroupInviteCode: String = "",
    val joinGroupIsLoading: Boolean = false
) {
    fun addGroupConfirmButtonIsEnabled(): Boolean {
        return !addGroupName.isBlank() && !addGroupIsLoading
    }

    fun joinGroupConfirmButtonIsEnabled(): Boolean {
        return joinGroupInviteCode.isNotBlank() && !joinGroupIsLoading
    }

    data class Group(
        val id: String,
        val name: String,
        val memberCount: Int
    )
}