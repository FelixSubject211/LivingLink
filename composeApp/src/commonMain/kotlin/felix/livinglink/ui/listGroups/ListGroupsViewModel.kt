package felix.livinglink.ui.listGroups

import ListGroupsScreenLocalizables
import felix.livinglink.common.model.LivingLinkError
import felix.livinglink.common.model.LivingLinkResult
import felix.livinglink.group.CreateGroupRequest
import felix.livinglink.group.CreateGroupResponse
import felix.livinglink.group.Group
import felix.livinglink.group.UseInviteRequest
import felix.livinglink.group.UseInviteResponse
import felix.livinglink.groups.repository.GroupsRepository
import felix.livinglink.ui.common.navigation.Navigator

class ListGroupsViewModel(
    override val navigator: Navigator,
    private val groupsRepository: GroupsRepository,
    private val viewModelState: ListGroupsViewModelState,
) : ListGroupsStatefulViewModel {
    override val loadableData = viewModelState.loadableData
    override val data = viewModelState.data
    override val error = viewModelState.error
    override val loading = viewModelState.loading

    override fun closeError() = viewModelState.closeError()

    fun showAddGroupDialog() = viewModelState.perform { data ->
        data.copy(showAddGroupDialog = true)
    }

    fun closeAddGroupDialog() = viewModelState.perform { data ->
        data.copy(showAddGroupDialog = false)
    }

    fun showJoinGroupDialog() = viewModelState.perform { data ->
        data.copy(showJoinGroupDialog = true)
    }

    fun closeJoinGroupDialog() = viewModelState.perform { data ->
        data.copy(showJoinGroupDialog = false)
    }

    fun createGroup(groupName: String) = viewModelState.perform(
        request = { _ -> groupsRepository.createGroup(CreateGroupRequest(groupName)) },
        onSuccess = { currentData, result ->
            when (result) {
                CreateGroupResponse.Error -> {
                    LivingLinkResult.Error(Error.CreateGroupResponseError)
                }

                is CreateGroupResponse.Success -> {
                    LivingLinkResult.Data(currentData)
                }
            }
        }
    )

    fun useInvite(code: String) = viewModelState.perform(
        request = { _ -> groupsRepository.useInvite(UseInviteRequest((code))) },
        onSuccess = { currentData, result ->
            when (result) {
                UseInviteResponse.InvalidOrAlreadyUsed -> {
                    LivingLinkResult.Error(Error.InviteInvalidOrAlreadyUsed)
                }

                UseInviteResponse.Success -> {
                    LivingLinkResult.Data(currentData)
                }
            }
        }
    )

    companion object {
        val initialState = Data(
            showAddGroupDialog = false,
            showJoinGroupDialog = false
        )
    }

    data class Data(
        val showAddGroupDialog: Boolean,
        val showJoinGroupDialog: Boolean
    )

    data class LoadableData(
        val groups: List<Group>
    )

    sealed class Error : LivingLinkError {
        data object InviteInvalidOrAlreadyUsed : Error() {
            override fun title() =
                ListGroupsScreenLocalizables.errorInviteInvalidOrAlreadyUsedTitle()
        }

        data object CreateGroupResponseError : Error() {
            override fun title() = ListGroupsScreenLocalizables.errorCreateGroupResponseTitle()
        }
    }
}