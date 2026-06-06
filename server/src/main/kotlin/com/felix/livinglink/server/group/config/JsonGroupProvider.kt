
package com.felix.livinglink.server.group.config

import com.felix.livinglink.server.core.config.Env
import com.felix.livinglink.server.group.domain.Group
import com.felix.livinglink.server.group.domain.GroupProvider
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import java.io.File

@Single(binds = [GroupProvider::class])
class JsonGroupProvider : GroupProvider {
    private val json = Json { ignoreUnknownKeys = true }

    private val groups: Map<String, Group> by lazy {
        val path = Env.required("LIVINGLINK_GROUPS_FILE")
        val file = File(path)
        require(file.exists()) { "Groups file not found at '$path' (LIVINGLINK_GROUPS_FILE)." }

        val config = json.decodeFromString<GroupsConfigDto>(file.readText())
        require(config.groups.isNotEmpty()) { "Groups file '$path' contains no groups." }

        val parsed = config.groups.map { it.toDomain() }
        val ids = parsed.map { it.id }
        require(ids.size == ids.toSet().size) { "Duplicate group id in '$path'. Group ids must be unique." }

        parsed.associateBy { it.id }
    }

    override fun groupsById(): Map<String, Group> = groups

    @Serializable
    private data class GroupsConfigDto(
        val groups: List<GroupDto>,
    )

    @Serializable
    private data class GroupDto(
        val id: String,
        val name: String,
        val memberUserIds: List<String>,
    ) {
        fun toDomain(): Group {
            require(id.isNotBlank()) { "Group id must not be blank." }
            require(name.isNotBlank()) { "Group '$id' name must not be blank." }
            return Group(
                id = id.trim(),
                name = name.trim(),
                memberUserIds = memberUserIds.map { it.trim() }.toSet(),
            )
        }
    }
}
