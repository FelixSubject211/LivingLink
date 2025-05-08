package felix.livinglink.group

import kotlinx.serialization.Serializable

@Serializable
data class GetGroupsForUserResponse(
    val groups: Set<Group>
)
