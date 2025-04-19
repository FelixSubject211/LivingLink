package felix.livinglink.group

import kotlinx.serialization.Serializable

@Serializable
sealed class DeleteGroupResponse {
    @Serializable
    data object Success : DeleteGroupResponse()

    @Serializable
    data object NotAllowed : DeleteGroupResponse()

    @Serializable
    data object Error : DeleteGroupResponse()
}