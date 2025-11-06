package felix.projekt.livinglink.server.groups.domain

sealed class CreateInviteCodeResponse {
    data class Success(val key: String) : CreateInviteCodeResponse()
}