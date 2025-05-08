package felix.livinglink.group

import kotlinx.serialization.Serializable

@Serializable
sealed class CreateGroupResponse {

    @Serializable
    data class Success(val groupId: String) : CreateGroupResponse()

    @Serializable
    data object Error : CreateGroupResponse()
}