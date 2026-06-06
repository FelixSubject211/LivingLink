package com.felix.livinglink.server.session.delivery.mcp.tools

import com.felix.livinglink.server.calendar.application.GetEventCategoriesUseCase
import com.felix.livinglink.server.core.delivery.mcp.dsl.McpToolDsl.tool
import com.felix.livinglink.server.core.delivery.mcp.dsl.success
import com.felix.livinglink.server.core.delivery.mcp.server.McpToolRegistrar
import com.felix.livinglink.server.group.application.GetActiveMcpGroupUseCase
import com.felix.livinglink.server.group.application.GetGroupsForUserUseCase
import com.felix.livinglink.server.group.delivery.mcp.dto.GroupReferenceMcpDto
import com.felix.livinglink.server.group.delivery.mcp.dto.toMcpReferenceDto
import com.felix.livinglink.server.user.application.FindUsersByIdsUseCase
import com.felix.livinglink.server.user.delivery.mcp.UserReferenceMcpDto
import com.felix.livinglink.server.user.delivery.mcp.toUserReferenceMcpDto
import io.modelcontextprotocol.kotlin.sdk.server.Server
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Single

@Single(binds = [McpToolRegistrar::class])
class GetSessionTool(
    private val findUsersByIdsUseCase: FindUsersByIdsUseCase,
    private val getEventCategoriesUseCase: GetEventCategoriesUseCase,
    private val getGroupsForUserUseCase: GetGroupsForUserUseCase,
    private val getActiveMcpGroupUseCase: GetActiveMcpGroupUseCase,
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
                Returns your current session context: who you are, the groups you belong to,
                and which group is currently active.

                All operations apply to the active group. Use set_active_group to switch.

                knownCustomEventCategoryLabels are scoped to the active group. Prefer reusing
                existing ones. Create new ones when it makes sense. Keep them in mind internally
                and do not display them to the user unless asked.
                """.trimIndent(),
        ) {
            handle {
                val groups = getGroupsForUserUseCase(userId)
                val activeGroup = getActiveMcpGroupUseCase(userId)

                val usersInActiveGroup =
                    activeGroup
                        ?.let { findUsersByIdsUseCase(it.memberUserIds) }
                        ?.values
                        ?.toList()
                        ?: emptyList()

                val labels =
                    activeGroup?.let {
                        getEventCategoriesUseCase(
                            GetEventCategoriesUseCase.Input(
                                byUserId = userId,
                                groupId = it.id,
                            ),
                        )
                    } ?: emptyList()

                success(
                    Output(
                        currentUserId = userId,
                        usersInActiveGroup = usersInActiveGroup.map { it.toUserReferenceMcpDto() },
                        availableGroups = groups.map { it.toMcpReferenceDto() },
                        activeGroupId = activeGroup?.id,
                        knownCustomEventCategoryLabels = labels,
                    ),
                )
            }
        }
    }

    @Serializable
    private data class Output(
        val currentUserId: String,
        val usersInActiveGroup: List<UserReferenceMcpDto>,
        val availableGroups: List<GroupReferenceMcpDto>,
        val activeGroupId: String?,
        val knownCustomEventCategoryLabels: List<String>,
    )
}
