package felix.projekt.livinglink.composeApp.ui.group.viewModel

sealed class GroupAction {
    object NavigateBack : GroupAction()
    object StartInviteCodeCreation : GroupAction()
    data class InviteCodeNameChanged(val name: String) : GroupAction()
    object SubmitInviteCodeCreation : GroupAction()
    object InviteCodeCreationCancelled : GroupAction()
    data class CopyInviteCode(val inviteCodeKey: String) : GroupAction()
    data class SubmitInviteCodeDeletion(val inviteCodeId: String) : GroupAction()
    object CancelInviteCodeDeletion : GroupAction()
    object ConfirmInviteCodeDeletion : GroupAction()
}