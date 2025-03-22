package felix.livinglink.auth

import kotlinx.serialization.Serializable

@Serializable
sealed class DeleteUserResponse {
    @Serializable
    data object Success : DeleteUserResponse()

    @Serializable
    data object Error : DeleteUserResponse()
}