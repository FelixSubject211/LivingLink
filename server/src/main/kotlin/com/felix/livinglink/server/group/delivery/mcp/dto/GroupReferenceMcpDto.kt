package com.felix.livinglink.server.group.delivery.mcp.dto

import com.felix.livinglink.server.group.domain.Group
import kotlinx.serialization.Serializable

@Serializable
data class GroupReferenceMcpDto(
    val id: String,
    val name: String,
)

fun Group.toMcpReferenceDto(): GroupReferenceMcpDto =
    GroupReferenceMcpDto(id = id, name = name)
