package felix.projekt.livinglink.composeApp.ui.group.viewModel

import felix.projekt.livinglink.composeApp.ui.group.viewModel.GroupState.InviteCode

sealed class GroupResult {
    data object GroupChangedToLoading : GroupResult()
    data class GroupsChanged(
        val groupId: String,
        val groupName: String,
        val memberIdToMemberName: Map<String, String>,
        val inviteCodes: List<InviteCode>
    ) : GroupResult()

    object InviteCodeCreationStarted : GroupResult()
    data class InviteCodeNameUpdated(val name: String) : GroupResult()
    object InviteCodeCreationSubmitting : GroupResult()
    object InviteCodeCreationCancelled : GroupResult()
    data class InviteCodeCreationSucceeded(val key: String) : GroupResult()
    data object InviteCodeCreationNetworkError : GroupResult()
    data class ShowDeleteInviteCodeConfirmation(val inviteCodeId: String) : GroupResult()
    data object CloseDeleteInviteCodeConfirmation : GroupResult()
    data object DeleteInviteCodeLoading : GroupResult()
    data object DeleteInviteCodeSuccess : GroupResult()
    data object DeleteInviteCodeError : GroupResult()
}