package com.felix.livinglink.server.shoppingList.delivery.mcp.tools

import com.felix.livinglink.server.core.delivery.mcp.dsl.McpToolDsl.tool
import com.felix.livinglink.server.core.delivery.mcp.dsl.success
import com.felix.livinglink.server.core.delivery.mcp.dsl.toolError
import com.felix.livinglink.server.core.delivery.mcp.server.McpToolRegistrar
import com.felix.livinglink.server.group.application.GetActiveMcpGroupUseCase
import com.felix.livinglink.server.shoppingList.application.DeleteShoppingListItemsUseCase
import io.modelcontextprotocol.kotlin.sdk.server.Server
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Single

@Single(binds = [McpToolRegistrar::class])
class DeleteShoppingListItemsTool(
    private val deleteShoppingListItemsUseCase: DeleteShoppingListItemsUseCase,
    private val getActiveMcpGroupUseCase: GetActiveMcpGroupUseCase,
) : McpToolRegistrar {
    override fun register(
        server: Server,
        userId: String,
    ) {
        server.tool(
            name = "delete_shopping_list_items",
            description = "Deletes one or more shopping list items",
        ) {
            val idsToDelete =
                required<Set<String>>(
                    name = "ids_to_delete",
                    description = "Deletes one or more shopping list items permanently",
                )

            handle {
                val group =
                    getActiveMcpGroupUseCase(userId)
                        ?: return@handle toolError("No group is available for this user.")

                val output =
                    deleteShoppingListItemsUseCase(
                        DeleteShoppingListItemsUseCase.Input(
                            byUserId = userId,
                            groupId = group.id,
                            idsToDelete = idsToDelete(),
                        ),
                    )

                success(
                    Output(
                        deletedIds = output.deletedIds,
                        missingIds = output.missingIds,
                    ),
                )
            }
        }
    }

    @Serializable
    private data class Output(
        val deletedIds: List<String>,
        val missingIds: List<String>,
    )
}
