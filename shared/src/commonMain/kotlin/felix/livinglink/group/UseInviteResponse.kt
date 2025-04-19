package felix.livinglink.group

import kotlinx.serialization.Serializable

@Serializable
sealed class UseInviteResponse {
    @Serializable
    data object Success : UseInviteResponse()

    @Serializable
    data object InvalidOrAlreadyUsed : UseInviteResponse()
}