package felix.livinglink.group

import kotlinx.serialization.Serializable

@Serializable
data class CreateInviteRequest(val groupId: String)