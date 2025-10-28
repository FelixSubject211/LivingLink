package felix.projekt.livinglink.composeApp.ui.group.viewModel

import felix.projekt.livinglink.composeApp.ui.core.viewmodel.Reducer

class GroupReducer : Reducer<GroupState, GroupResult> {
    override fun invoke(
        state: GroupState,
        result: GroupResult
    ) = when (result) {
        is GroupResult.GroupChangedToLoading -> {
            state.copy(
                groupId = null,
                groupName = null,
                groupIsLoading = true
            )
        }

        is GroupResult.GroupsChanged -> {
            state.copy(
                groupId = result.groupId,
                groupName = result.groupName,
                groupIsLoading = false
            )
        }
    }
}