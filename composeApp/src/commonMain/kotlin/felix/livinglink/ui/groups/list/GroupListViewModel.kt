package felix.livinglink.ui.groups.list

import GroupListScreenLocalizables
import felix.livinglink.common.model.LivingLinkError
import felix.livinglink.common.model.LivingLinkResult
import felix.livinglink.group.CreateGroupRequest
import felix.livinglink.group.CreateGroupResponse
import felix.livinglink.group.Group
import felix.livinglink.group.UseInviteRequest
import felix.livinglink.group.UseInviteResponse
import felix.livinglink.groups.repository.GroupsRepository
import felix.livinglink.ui.common.navigation.Navigator

class GroupListViewModel(
    override val navigator: Navigator,
    private val groupsRepository: GroupsRepository,
    private val viewModelState: GroupListViewModelState,
) : GroupListStatefulViewModel {
    override val loadableData = viewModelState.loadableData
    override val data = viewModelState.data
    override val error = viewModelState.error
    override val loading = viewModelState.loading
    override fun closeError() = viewModelState.closeError()
    override fun cancel() = viewModelState.cancel()

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
                    LivingLinkResult.Success(currentData)
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
                    LivingLinkResult.Success(currentData)
                }
            }
        }
    )

    fun showDeleteGroupDialog(groupId: String) = viewModelState.perform { data ->
        data.copy(groupIdToDelete = groupId)
    }

    fun closeDeleteDialog() = viewModelState.perform { data ->
        data.copy(groupIdToDelete = null)
    }

    fun deleteGroup() = viewModelState.perform(
        request = { groupsRepository.deleteGroup(groupId = it.groupIdToDelete!!) },
        onSuccess = { currentData, _ ->
            LivingLinkResult.Success(
                currentData.copy(groupIdToDelete = null)
            )
        }
    )

    companion object {
        val initialState = Data(
            showAddGroupDialog = false,
            showJoinGroupDialog = false,
            groupIdToDelete = null
        )
    }

    data class Data(
        val showAddGroupDialog: Boolean,
        val showJoinGroupDialog: Boolean,
        val groupIdToDelete: String?
    )

    data class LoadableData(
        val groups: List<Group>
    )

    sealed class Error : LivingLinkError {
        data object InviteInvalidOrAlreadyUsed : Error() {
            override fun title() =
                GroupListScreenLocalizables.errorInviteInvalidOrAlreadyUsedTitle()
        }

        data object CreateGroupResponseError : Error() {
            override fun title() =
                GroupListScreenLocalizables.errorCreateGroupResponseTitle()
        }
    }
}