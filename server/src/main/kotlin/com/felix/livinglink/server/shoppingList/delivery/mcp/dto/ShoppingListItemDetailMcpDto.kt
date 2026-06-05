package com.felix.livinglink.server.shoppingList.delivery.mcp.dto

import com.felix.livinglink.server.core.config.TimezoneSettings
import com.felix.livinglink.server.core.delivery.mcp.dsl.toMcpString
import com.felix.livinglink.server.core.domain.User
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItem
import com.felix.livinglink.server.user.delivery.mcp.UserReferenceMcpDto
import com.felix.livinglink.server.user.delivery.mcp.toUserReferenceMcpDto
import kotlinx.serialization.Serializable

@Serializable
data class ShoppingListItemDetailMcpDto(
    val id: String,
    val name: String,
    val createdBy: UserReferenceMcpDto?,
    val completed: Boolean,
    val completionEvents: List<CompletionEventMcpDto>,
    val createdAt: String,
    val updatedAt: String,
) {
    @Serializable
    data class CompletionEventMcpDto(
        val by: UserReferenceMcpDto?,
        val at: String,
    )
}

fun ShoppingListItem.toMcpDetailDto(
    usersById: Map<String, User>,
    timezoneSettings: TimezoneSettings,
): ShoppingListItemDetailMcpDto =
    ShoppingListItemDetailMcpDto(
        id = id,
        name = name,
        completed = isCompleted,
        completionEvents =
            completionEvents.map { event ->
                ShoppingListItemDetailMcpDto.CompletionEventMcpDto(
                    by = usersById[event.byUserId]?.toUserReferenceMcpDto(),
                    at = event.at.toMcpString(timezoneSettings),
                )
            },
        createdBy = usersById[createdByUserId]?.toUserReferenceMcpDto(),
        createdAt = createdAt.toMcpString(timezoneSettings),
        updatedAt = updatedAt.toMcpString(timezoneSettings),
    )
