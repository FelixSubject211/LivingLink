package felix.livinglink.group

import kotlinx.serialization.Serializable

@Serializable
data class CreateGroupRequest(val groupName: String)
