package felix.livinglink.group

import kotlinx.serialization.Serializable

@Serializable
sealed class RemoveUserFromGroupResponse {
    @Serializable
    data object Success : RemoveUserFromGroupResponse()

    @Serializable
    data object NotAllowed : RemoveUserFromGroupResponse()

    @Serializable
    data object Error : RemoveUserFromGroupResponse()
}
