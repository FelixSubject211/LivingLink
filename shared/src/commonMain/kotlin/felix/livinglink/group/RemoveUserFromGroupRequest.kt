package felix.livinglink.group

import kotlinx.serialization.Serializable

@Serializable
data class RemoveUserFromGroupRequest(
    val groupId: String,
    val userId: String
)
