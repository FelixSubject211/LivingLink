package felix.projekt.livinglink.server.groups.domain

sealed class DeleteInviteCodeResponse {
    data object Success : DeleteInviteCodeResponse()
}