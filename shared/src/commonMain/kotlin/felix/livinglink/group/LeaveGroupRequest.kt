package felix.livinglink.group

import kotlinx.serialization.Serializable

@Serializable
data class LeaveGroupRequest(val groupId: String)
