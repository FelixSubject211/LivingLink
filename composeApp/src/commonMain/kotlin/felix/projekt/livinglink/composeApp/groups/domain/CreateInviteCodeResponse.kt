package felix.projekt.livinglink.composeApp.groups.domain

sealed class CreateInviteCodeResponse {
    data class Success(val key: String) : CreateInviteCodeResponse()
}