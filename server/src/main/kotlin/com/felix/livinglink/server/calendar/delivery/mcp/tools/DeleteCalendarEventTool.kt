package com.felix.livinglink.server.calendar.delivery.mcp.tools

import com.felix.livinglink.server.calendar.application.DeleteCalendarEventUseCase
import com.felix.livinglink.server.core.delivery.mcp.dsl.McpToolDsl.tool
import com.felix.livinglink.server.core.delivery.mcp.dsl.success
import com.felix.livinglink.server.core.delivery.mcp.dsl.toolError
import com.felix.livinglink.server.core.delivery.mcp.server.McpToolRegistrar
import com.felix.livinglink.server.group.application.GetActiveMcpGroupUseCase
import io.modelcontextprotocol.kotlin.sdk.server.Server
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Single

@Single(binds = [McpToolRegistrar::class])
class DeleteCalendarEventTool(
    private val deleteCalendarEventUseCase: DeleteCalendarEventUseCase,
    private val getActiveMcpGroupUseCase: GetActiveMcpGroupUseCase,
) : McpToolRegistrar {
    override fun register(
        server: Server,
        userId: String,
    ) {
        server.tool(
            name = "delete_calendar_event",
            description = "Deletes a single calendar event permanently.",
        ) {
            val eventId =
                required<String>(
                    name = "event_id",
                    description = "The id of the calendar event to delete.",
                )

            handle {
                val group =
                    getActiveMcpGroupUseCase(userId)
                        ?: return@handle toolError("No group is available for this user.")

                when (
                    deleteCalendarEventUseCase(
                        DeleteCalendarEventUseCase.Input(
                            byUserId = userId,
                            groupId = group.id,
                            eventId = eventId(),
                        ),
                    )
                ) {
                    is DeleteCalendarEventUseCase.Output.Deleted ->
                        success(Output(deletedEventId = eventId()))

                    is DeleteCalendarEventUseCase.Output.NotFound ->
                        toolError("Calendar event '${eventId()}' not found.")
                }
            }
        }
    }

    @Serializable
    private data class Output(
        val deletedEventId: String,
    )
}
