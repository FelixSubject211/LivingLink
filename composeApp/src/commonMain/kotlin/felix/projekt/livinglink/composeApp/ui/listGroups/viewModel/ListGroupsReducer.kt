package felix.projekt.livinglink.composeApp.ui.listGroups.viewModel

import felix.projekt.livinglink.composeApp.ui.core.viewmodel.Reducer

class ListGroupsReducer : Reducer<ListGroupsState, ListGroupsResult> {
    override fun invoke(
        state: ListGroupsState,
        result: ListGroupsResult
    ) = when (result) {
        ListGroupsResult.GroupsChangedToLoading -> {
            state.copy(groupsLoading = true)
        }

        is ListGroupsResult.GroupsChanged -> {
            state.copy(
                groups = result.groups,
                groupsLoading = false
            )
        }

        is ListGroupsResult.MenuExpanded -> {
            state.copy(menuExpanded = true)
        }

        is ListGroupsResult.MenuClosed -> {
            state.copy(menuExpanded = false)
        }

        is ListGroupsResult.ShowAddGroupDialog -> {
            state.copy(
                menuExpanded = false,
                showAddGroup = true
            )
        }

        is ListGroupsResult.AddGroupNameChanged -> {
            state.copy(addGroupName = result.value)
        }

        is ListGroupsResult.ConfirmAddGroup -> {
            state.copy(
                addGroupName = "",
                addGroupIsLoading = true
            )
        }

        ListGroupsResult.CloseAddGroupDialog -> {
            state.copy(
                showAddGroup = false,
                addGroupName = ""
            )
        }

        ListGroupsResult.AddGroupFinished -> {
            state.copy(
                showAddGroup = false,
                addGroupIsLoading = false
            )
        }
    }
}