package felix.livinglink.group

import kotlinx.serialization.Serializable

@Serializable
data class MakeUserAdminRequest(
    val groupId: String,
    val userId: String
)
