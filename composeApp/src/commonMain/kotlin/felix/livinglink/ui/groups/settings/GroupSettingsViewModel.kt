package felix.livinglink.ui.groups.settings

import CommonLocalizables
import GroupsSettingsScreenLocalizables
import felix.livinglink.common.model.LivingLinkError
import felix.livinglink.common.model.LivingLinkResult
import felix.livinglink.group.CreateInviteRequest
import felix.livinglink.group.CreateInviteResponse
import felix.livinglink.group.Group
import felix.livinglink.group.LeaveGroupResponse
import felix.livinglink.groups.repository.GroupsRepository
import felix.livinglink.ui.common.navigation.Navigator

class GroupSettingsViewModel(
    val groupId: String,
    override val navigator: Navigator,
    private val groupsRepository: GroupsRepository,
    private val viewModelState: GroupSettingsViewModelState
) : GroupSettingsStatefulViewModel {
    override val loadableData = viewModelState.loadableData
    override val data = viewModelState.data
    override val error = viewModelState.error
    override val loading = viewModelState.loading
    override fun closeError() = viewModelState.closeError()
    override fun cancel() = viewModelState.cancel()

    fun createInviteCode() = viewModelState.perform(
        request = { groupsRepository.createInvite(CreateInviteRequest(groupId)) },
        onSuccess = { currentData, result ->
            when (result) {
                CreateInviteResponse.Error -> {
                    LivingLinkResult.Error(
                        Error.UnknownError
                    )
                }
                is CreateInviteResponse.Success -> {
                    LivingLinkResult.Success(
                        currentData.copy(inviteCode = result.code)
                    )
                }
            }
        }
    )

    fun closeInviteCode() = viewModelState.perform { data ->
        data.copy(inviteCode = null)
    }

    fun showDeleteGroupDialog() = viewModelState.perform { data ->
        data.copy(showDeleteGroupDialog = true)
    }

    fun closeDeleteGroupDialog() = viewModelState.perform { data ->
        data.copy(showDeleteGroupDialog = false)
    }

    fun deleteGroup() = viewModelState.perform(
        request = { groupsRepository.deleteGroup(groupId = groupId) },
        onSuccess = { currentData, _ ->
            navigator.pop()
            LivingLinkResult.Success(
                currentData.copy(showDeleteGroupDialog = false)
            )
        }
    )

    fun showLeaveGroupDialog() = viewModelState.perform { data ->
        data.copy(showLeaveGroupDialog = true)
    }

    fun closeLeaveGroupDialog() = viewModelState.perform { data ->
        data.copy(showLeaveGroupDialog = false)
    }

    fun leaveGroup() = viewModelState.perform(
        request = { groupsRepository.leaveGroup(groupId = groupId) },
        onSuccess = { currentData, result ->
            when (result) {
                LeaveGroupResponse.LastAdminCannotLeave -> {
                    LivingLinkResult.Error(Error.LastAdminCannotLeave)
                }

                else -> {
                    navigator.pop()
                    LivingLinkResult.Success(
                        currentData.copy(showLeaveGroupDialog = false)
                    )
                }
            }
        }
    )

    companion object {
        val initialState = Data(
            showDeleteGroupDialog = false,
            showLeaveGroupDialog = false,
            inviteCode = null
        )
    }

     data class Data(
        val showDeleteGroupDialog: Boolean,
        val showLeaveGroupDialog: Boolean,
        val inviteCode: String?
    )

    data class LoadableData(
        val group: Group,
        val currentUserId: String
    )

    sealed class Error : LivingLinkError {
        data object LastAdminCannotLeave : Error() {
            override fun title() =
                GroupsSettingsScreenLocalizables.errorLastAdminCannotLeaveTitle()
        }

        data object UnknownError : Error() {
            override fun title() = CommonLocalizables.unknownErrorTitle()
        }
    }
}