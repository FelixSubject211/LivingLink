package felix.livinglink.ui.groups.list

import GroupsListScreenLocalizables
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
        data.copy(
            showAddGroupDialog = false,
            addGroupName = ""
        )
    }

    fun updateAddGroupName(groupName: String) {
        viewModelState.perform { currentData ->
            currentData.copy(addGroupName = groupName)
        }
    }

    fun createGroupConfirmButtonEnabled(): Boolean {
        return data.value.addGroupName.isNotBlank()
    }

    fun createGroup() = viewModelState.perform(
        request = { currentData ->
            groupsRepository.createGroup(CreateGroupRequest(currentData.addGroupName))
        },
        onSuccess = { currentData, result ->
            when (result) {
                CreateGroupResponse.Error -> {
                    LivingLinkResult.Error(Error.CreateGroupResponseError)
                }

                is CreateGroupResponse.Success -> {
                    LivingLinkResult.Success(
                        currentData.copy(
                            showAddGroupDialog = false,
                            addGroupName = ""
                        )
                    )
                }
            }
        }
    )

    fun showJoinGroupDialog() = viewModelState.perform { data ->
        data.copy(showJoinGroupDialog = true)
    }

    fun closeJoinGroupDialog() = viewModelState.perform { data ->
        data.copy(
            showJoinGroupDialog = false,
            inviteCode = ""
        )
    }

    fun useInvite() = viewModelState.perform(
        request = { currentData ->
            groupsRepository.useInvite(UseInviteRequest((currentData.inviteCode)))
        },
        onSuccess = { currentData, result ->
            when (result) {
                UseInviteResponse.InvalidOrAlreadyUsed -> {
                    LivingLinkResult.Error(Error.InviteInvalidOrAlreadyUsed)
                }

                UseInviteResponse.Success -> {
                    LivingLinkResult.Success(
                        currentData.copy(
                            showJoinGroupDialog = false,
                            inviteCode = ""
                        )
                    )
                }
            }
        }
    )

    fun updateInviteCode(inviteCode: String) {
        viewModelState.perform { currentData ->
            currentData.copy(inviteCode = inviteCode)
        }
    }

    fun useInviteConfirmButtonEnabled(): Boolean {
        return data.value.inviteCode.isNotBlank()
    }

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
            addGroupName = "",
            showJoinGroupDialog = false,
            inviteCode = "",
            groupIdToDelete = null,
        )
    }

    data class Data(
        val showAddGroupDialog: Boolean,
        val addGroupName: String,
        val showJoinGroupDialog: Boolean,
        val inviteCode: String,
        val groupIdToDelete: String?
    )

    data class LoadableData(
        val groups: List<Group>
    )

    sealed class Error : LivingLinkError {
        data object InviteInvalidOrAlreadyUsed : Error() {
            override fun title() =
                GroupsListScreenLocalizables.errorInviteInvalidOrAlreadyUsedTitle()
        }

        data object CreateGroupResponseError : Error() {
            override fun title() =
                GroupsListScreenLocalizables.errorCreateGroupResponseTitle()
        }
    }
}