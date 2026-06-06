package com.felix.livinglink.server.calendar.delivery.mcp.tools

import com.felix.livinglink.server.calendar.application.AddCalendarEventUseCase
import com.felix.livinglink.server.calendar.delivery.mcp.dto.CalendarEventReferenceMcpDto
import com.felix.livinglink.server.calendar.delivery.mcp.dto.EventCategoryMcpDto
import com.felix.livinglink.server.calendar.delivery.mcp.dto.EventSpanMcpDto
import com.felix.livinglink.server.calendar.delivery.mcp.dto.RecurrenceRuleMcpDto
import com.felix.livinglink.server.calendar.delivery.mcp.dto.toMcpReferenceDto
import com.felix.livinglink.server.core.delivery.mcp.dsl.McpToolDsl.tool
import com.felix.livinglink.server.core.delivery.mcp.dsl.success
import com.felix.livinglink.server.core.delivery.mcp.dsl.toolError
import com.felix.livinglink.server.core.delivery.mcp.server.McpToolRegistrar
import com.felix.livinglink.server.group.application.GetActiveMcpGroupUseCase
import io.modelcontextprotocol.kotlin.sdk.server.Server
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Single

@Single(binds = [McpToolRegistrar::class])
class AddCalendarEventTool(
    private val addCalendarEventUseCase: AddCalendarEventUseCase,
    private val getActiveMcpGroupUseCase: GetActiveMcpGroupUseCase,
) : McpToolRegistrar {
    override fun register(
        server: Server,
        userId: String,
    ) {
        server.tool(
            name = "add_calendar_event",
            description = "Adds a calendar event. Set 'recurrence' to make it repeat.",
        ) {
            val title =
                required<String>(
                    name = "title",
                    description = "Title of the event.",
                )

            val description =
                optional<String>(
                    name = "description",
                    description = "Optional description.",
                )

            val span =
                required<EventSpanMcpDto>(
                    name = "span",
                    description = "Time span: either type='timed' with ISO 8601 start/end, or type='allDay' with ISO 8601 start/end.",
                )

            val recurrence =
                optional<RecurrenceRuleMcpDto>(
                    name = "recurrence",
                    description = "Optional recurrence rule. Omit for one-off events.",
                )

            val participantUserIds =
                optional<Set<String>>(
                    name = "participantUserIds",
                    description = "List of participant user IDs. Use user IDs from get_session.",
                )

            val category =
                optional<EventCategoryMcpDto>(
                    name = "category",
                    description = "Optional category. For type='custom', see get_session for known labels.",
                    default = EventCategoryMcpDto.None,
                )

            handle {
                val group =
                    getActiveMcpGroupUseCase(userId)
                        ?: return@handle toolError("No group is available for this user.")

                val event =
                    addCalendarEventUseCase(
                        AddCalendarEventUseCase.Input(
                            byUserId = userId,
                            groupId = group.id,
                            title = title(),
                            description = description(),
                            span = span().toDomain(),
                            recurrence = recurrence()?.toDomain(),
                            category = category().toDomain(),
                            participantUserIds = participantUserIds()?.toSet() ?: emptySet(),
                        ),
                    )

                success(
                    Output(
                        addedEvent = event.toMcpReferenceDto(),
                    ),
                )
            }
        }
    }

    @Serializable
    private data class Output(
        val addedEvent: CalendarEventReferenceMcpDto,
    )
}
