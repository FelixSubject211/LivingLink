package com.felix.livinglink.server.user.delivery.mcp

import com.felix.livinglink.server.core.domain.User
import kotlinx.serialization.Serializable

@Serializable
data class UserReferenceMcpDto(
    val id: String,
    val username: String,
)

fun User.toUserReferenceMcpDto() =
    UserReferenceMcpDto(
        id = this.id,
        username = this.username,
    )
