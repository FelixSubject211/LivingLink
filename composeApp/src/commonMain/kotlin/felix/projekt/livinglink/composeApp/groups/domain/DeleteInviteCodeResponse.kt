package felix.projekt.livinglink.composeApp.groups.domain

sealed class DeleteInviteCodeResponse {
    data object Success : DeleteInviteCodeResponse()
}