package com.felix.livinglink.server.group.delivery.mcp.tools

import com.felix.livinglink.server.core.delivery.mcp.dsl.McpToolDsl.tool
import com.felix.livinglink.server.core.delivery.mcp.dsl.success
import com.felix.livinglink.server.core.delivery.mcp.server.McpToolRegistrar
import com.felix.livinglink.server.group.application.SetActiveGroupUseCase
import com.felix.livinglink.server.group.delivery.mcp.dto.GroupReferenceMcpDto
import com.felix.livinglink.server.group.delivery.mcp.dto.toMcpReferenceDto
import io.modelcontextprotocol.kotlin.sdk.server.Server
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Single

@Single(binds = [McpToolRegistrar::class])
class SetActiveGroupTool(
    private val setActiveGroupUseCase: SetActiveGroupUseCase,
) : McpToolRegistrar {
    override fun register(server: Server, userId: String) {
        server.tool(
            name = "set_active_group",
            description =
                "Sets the active group for this user. All subsequent operations apply to " +
                    "this group until changed. Use a group id from get_session.",
        ) {
            val groupId =
                required<String>(
                    name = "group_id",
                    description = "The id of the group to activate. See availableGroups in get_session.",
                )

            handle {
                val group = setActiveGroupUseCase(userId = userId, groupId = groupId())
                success(Output(activeGroup = group.toMcpReferenceDto()))
            }
        }
    }

    @Serializable
    private data class Output(
        val activeGroup: GroupReferenceMcpDto,
    )
}
