package felix.livinglink.ui.groups.settings

import felix.livinglink.common.model.LivingLinkResult
import felix.livinglink.group.CreateInviteRequest
import felix.livinglink.group.Group
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
            LivingLinkResult.Success(
                currentData.copy(inviteCode = result.code)
            )
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

    companion object {
        val initialState = Data(
            showDeleteGroupDialog = false,
            inviteCode = null
        )
    }

    data class Data(
        val showDeleteGroupDialog: Boolean,
        val inviteCode: String?
    )

    data class LoadableData(
        val group: Group
    )
}