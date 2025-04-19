package felix.livinglink.group

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Group(
    val id: String,
    val name: String,
    val groupMemberIdsToName: Map<String, String>,
    val createdAt: Instant
)