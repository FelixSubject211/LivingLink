package com.felix.livinglink.server.session.delivery.mcp.tools

import com.felix.livinglink.server.calendar.application.GetEventCategoriesUseCase
import com.felix.livinglink.server.core.delivery.mcp.dsl.McpToolDsl.tool
import com.felix.livinglink.server.core.delivery.mcp.dsl.success
import com.felix.livinglink.server.core.delivery.mcp.server.McpToolRegistrar
import com.felix.livinglink.server.user.application.GetAllUsersUseCase
import com.felix.livinglink.server.user.delivery.mcp.UserReferenceMcpDto
import com.felix.livinglink.server.user.delivery.mcp.toUserReferenceMcpDto
import io.modelcontextprotocol.kotlin.sdk.server.Server
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Single

@Single(binds = [McpToolRegistrar::class])
class GetSessionTool(
    private val getAllUsersUseCase: GetAllUsersUseCase,
    private val getEventCategoriesUseCase: GetEventCategoriesUseCase,
) : McpToolRegistrar {
    override fun register(
        server: Server,
        userId: String,
    ) {
        server.tool(
            name = "get_session",
            description =
                """
                Call this tool first before doing anything else.
                Returns your current session context, including who you are.

                The response includes known custom calendar event category labels.
                Prefer reusing existing ones for consistency, but feel free to create new ones when it makes sense.
                Keep them in mind internally and do not display them to the user unless asked.
                """.trimIndent(),
        ) {
            handle {
                val allUsers = getAllUsersUseCase()
                val knownCustomEventCategoryLabels = getEventCategoriesUseCase()

                success(
                    Output(
                        currentUserId = userId,
                        availableUsers = allUsers.map { user -> user.toUserReferenceMcpDto() },
                        knownCustomEventCategoryLabels = knownCustomEventCategoryLabels,
                    ),
                )
            }
        }
    }

    @Serializable
    private data class Output(
        val currentUserId: String,
        val availableUsers: List<UserReferenceMcpDto>,
        val knownCustomEventCategoryLabels: List<String>,
    )
}
