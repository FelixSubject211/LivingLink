package com.felix.livinglink.server.calendar.delivery.mcp.dto

import com.felix.livinglink.server.calendar.domain.ScheduledEvent
import com.felix.livinglink.server.core.config.TimezoneSettings
import com.felix.livinglink.server.core.delivery.mcp.dsl.toMcpString
import com.felix.livinglink.server.core.domain.User
import com.felix.livinglink.server.user.delivery.mcp.UserReferenceMcpDto
import com.felix.livinglink.server.user.delivery.mcp.toUserReferenceMcpDto
import kotlinx.serialization.Serializable

@Serializable
data class ScheduledEventDetailMcpDto(
    val sourceEventId: String,
    val title: String,
    val description: String?,
    val createdBy: UserReferenceMcpDto?,
    val span: EventSpanMcpDto,
    val participants: List<ParticipantMcpDto>,
    val category: EventCategoryMcpDto,
    val createdAt: String,
    val updatedAt: String,
) {
    @Serializable
    data class ParticipantMcpDto(
        val user: UserReferenceMcpDto?,
        val currentStatus: RsvpStatusMcpDto,
    )
}

fun ScheduledEvent.toMcpDetailDto(
    usersById: Map<String, User>,
    timezoneSettings: TimezoneSettings,
): ScheduledEventDetailMcpDto =
    ScheduledEventDetailMcpDto(
        sourceEventId = sourceEventId,
        title = title,
        description = description,
        createdBy = usersById[createdByUserId]?.toUserReferenceMcpDto(),
        span = EventSpanMcpDto.fromDomain(span),
        participants =
            participants.map { participant ->
                ScheduledEventDetailMcpDto.ParticipantMcpDto(
                    user = usersById[participant.userId]?.toUserReferenceMcpDto(),
                    currentStatus = RsvpStatusMcpDto.fromDomain(participant.currentStatus),
                )
            },
        category = EventCategoryMcpDto.fromDomain(category),
        createdAt = createdAt.toMcpString(timezoneSettings),
        updatedAt = updatedAt.toMcpString(timezoneSettings),
    )
