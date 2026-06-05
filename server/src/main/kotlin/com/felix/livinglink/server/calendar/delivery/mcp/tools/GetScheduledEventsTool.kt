package com.felix.livinglink.server.calendar.delivery.mcp.tools

import com.felix.livinglink.server.calendar.application.GetScheduledEventsUseCase
import com.felix.livinglink.server.calendar.delivery.mcp.dto.CalendarEventSortMcpDto
import com.felix.livinglink.server.calendar.delivery.mcp.dto.ScheduledEventDetailMcpDto
import com.felix.livinglink.server.calendar.delivery.mcp.dto.toMcpDetailDto
import com.felix.livinglink.server.calendar.domain.CalendarEventQuery
import com.felix.livinglink.server.core.config.TimezoneSettings
import com.felix.livinglink.server.core.delivery.mcp.dsl.McpToolDsl.tool
import com.felix.livinglink.server.core.delivery.mcp.dsl.success
import com.felix.livinglink.server.core.delivery.mcp.server.McpToolRegistrar
import com.felix.livinglink.server.user.application.FindUsersByIdsUseCase
import io.modelcontextprotocol.kotlin.sdk.server.Server
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Single

@Single(binds = [McpToolRegistrar::class])
class GetScheduledEventsTool(
    private val getScheduledEventsUseCase: GetScheduledEventsUseCase,
    private val findUsersByIdsUseCase: FindUsersByIdsUseCase,
    private val timezoneSettings: TimezoneSettings,
) : McpToolRegistrar {
    override fun register(
        server: Server,
        userId: String,
    ) {
        server.tool(
            name = "get_scheduled_calendar_events",
            description = "Get calendar events that intersect a time range. Recurring events are expanded into individual occurrences.",
        ) {
            val from =
                requiredInstant(
                    name = "from",
                    description = "Start of the time range as ISO 8601 instant.",
                )

            val to =
                requiredInstant(
                    name = "to",
                    description = "End of the time range as ISO 8601 instant.",
                )

            val participantUserIds =
                optional<Set<String>>(
                    name = "participantUserIds",
                    description = "Filter: only events with at least one of these participants. Use user IDs from get_session.",
                )

            val createdByUserIds =
                optional<Set<String>>(
                    name = "createdByUserIds",
                    description = "Filter: only events created by one of these users. Use user IDs from get_session.",
                )

            val sort =
                optional<CalendarEventSortMcpDto>(
                    name = "sort",
                    description = "Sort order.",
                    default = CalendarEventSortMcpDto.EffectiveStartAscending,
                )

            handle {
                val output =
                    getScheduledEventsUseCase(
                        GetScheduledEventsUseCase.Input(
                            query =
                                CalendarEventQuery(
                                    from = from(),
                                    to = to(),
                                    participantUserIds = participantUserIds()?.toSet(),
                                    createdByUserIds = createdByUserIds()?.toSet(),
                                ),
                            sort = sort().toDomain(),
                            timeZone = timezoneSettings.timeZone,
                        ),
                    )

                val usersById =
                    findUsersByIdsUseCase(
                        ids = output.scheduledEvents.flatMap { it.referencedUserIds },
                    )

                success(
                    Output(
                        scheduledEvents =
                            output.scheduledEvents.map { event ->
                                event.toMcpDetailDto(
                                    usersById = usersById,
                                    timezoneSettings = timezoneSettings,
                                )
                            },
                    ),
                )
            }
        }
    }

    @Serializable
    private data class Output(
        val scheduledEvents: List<ScheduledEventDetailMcpDto>,
    )
}
