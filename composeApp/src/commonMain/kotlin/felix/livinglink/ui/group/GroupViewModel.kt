package felix.livinglink.ui.group

import felix.livinglink.common.model.LivingLinkError
import felix.livinglink.common.model.LivingLinkResult
import felix.livinglink.group.CreateInviteRequest
import felix.livinglink.group.Group
import felix.livinglink.groups.repository.GroupsRepository
import felix.livinglink.ui.common.navigation.Navigator

class GroupViewModel(
    override val navigator: Navigator,
    private val groupId: String,
    private val groupsRepository: GroupsRepository,
    private val viewModelState: GroupViewModelState,
) : GroupStatefulViewModel {
    override val loadableData = viewModelState.loadableData
    override val data = viewModelState.data
    override val error = viewModelState.error
    override val loading = viewModelState.loading
    override fun closeError() = viewModelState.closeError()
    override fun cancel() = viewModelState.cancel()

    fun expandMenu() = viewModelState.perform { data ->
        data.copy(menuExpanded = true)
    }

    fun closeMenu() = viewModelState.perform { data ->
        data.copy(menuExpanded = false)
    }

    fun showDeleteGroupDialog() = viewModelState.perform { data ->
        data.copy(
            menuExpanded = false,
            showDeleteGroupDialog = true
        )
    }

    fun createInviteCode() = viewModelState.perform(
        request = { groupsRepository.createInvite(CreateInviteRequest(groupId)) },
        onSuccess = { currentData, result ->
            LivingLinkResult.Success(
                currentData.copy(
                    menuExpanded = false,
                    inviteCode = result.code
                )
            )
        }
    )

    fun closeDeleteGroupDialog() = viewModelState.perform { data ->
        data.copy(showDeleteGroupDialog = false)
    }

    fun closeInviteCode() = viewModelState.perform { data ->
        data.copy(inviteCode = null)
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
            menuExpanded = false,
            showDeleteGroupDialog = false,
            inviteCode = null
        )
    }

    data class Data(
        val menuExpanded: Boolean,
        val showDeleteGroupDialog: Boolean,
        val inviteCode: String?
    )

    data class LoadableData(
        val group: Group
    )

    sealed class Error : LivingLinkError
}