package felix.livinglink.group

import kotlinx.serialization.Serializable

@Serializable
sealed class CreateInviteResponse {
    @Serializable
    data class Success(val code: String) : CreateInviteResponse()

    @Serializable
    data object Error : CreateInviteResponse()
}