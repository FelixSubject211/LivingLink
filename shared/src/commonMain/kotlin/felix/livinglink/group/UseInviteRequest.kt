package felix.livinglink.group

import kotlinx.serialization.Serializable

@Serializable
data class UseInviteRequest(val code: String)