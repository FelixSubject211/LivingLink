package felix.livinglink.group

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Group(
    val id: String,
    val name: String,
    val groupMemberIdsToName: Map<String, String>,
    val createdAt: Instant,
    val adminUserIds: Set<String> = emptySet()
) {
    @Transient
    val groupMembersSortedByRoleAndName = groupMemberIdsToName
        .map { (id, name) ->
            GroupMember(
                id = id,
                name = name,
                isAdmin = adminUserIds.contains(id)
            )
        }
        .sortedWith(
            compareByDescending<GroupMember> { it.isAdmin }
                .thenBy { it.name.lowercase() }
        )

    data class GroupMember(
        val id: String,
        val name: String,
        val isAdmin: Boolean
    )
}