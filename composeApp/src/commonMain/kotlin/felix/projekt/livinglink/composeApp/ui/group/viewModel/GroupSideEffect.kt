package felix.projekt.livinglink.composeApp.ui.group.viewModel

sealed class GroupSideEffect {
    sealed class ShowSnackbar : GroupSideEffect() {
        data object InviteCodeCopied : ShowSnackbar()
        data object DeleteInviteCodeNetworkError : ShowSnackbar()
    }

    sealed class CopyToClipboard : GroupSideEffect() {
        data class CopiedInviteCode(val inviteCodeKey: String) : CopyToClipboard()
    }

    object NavigateBack : GroupSideEffect()
}