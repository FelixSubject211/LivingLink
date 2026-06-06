package com.felix.livinglink.server.shoppingList.delivery.mcp.tools

import com.felix.livinglink.server.core.delivery.mcp.dsl.McpToolDsl.tool
import com.felix.livinglink.server.core.delivery.mcp.dsl.success
import com.felix.livinglink.server.core.delivery.mcp.dsl.toolError
import com.felix.livinglink.server.core.delivery.mcp.server.McpToolRegistrar
import com.felix.livinglink.server.group.application.GetActiveMcpGroupUseCase
import com.felix.livinglink.server.shoppingList.application.ChangeShoppingListItemsCompleteStateUseCase
import com.felix.livinglink.server.shoppingList.delivery.mcp.dto.ShoppingListItemReferenceMcpDto
import com.felix.livinglink.server.shoppingList.delivery.mcp.dto.toMcpReferenceDto
import io.modelcontextprotocol.kotlin.sdk.server.Server
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Single

@Single(binds = [McpToolRegistrar::class])
class ChangeShoppingListItemsCompleteStateTool(
    private val changeShoppingListItemsCompleteStateUseCase: ChangeShoppingListItemsCompleteStateUseCase,
    private val getActiveMcpGroupUseCase: GetActiveMcpGroupUseCase,
) : McpToolRegistrar {
    override fun register(
        server: Server,
        userId: String,
    ) {
        server.tool(
            name = "change_shopping_list_items_complete_state",
            description = "Marks one or more shopping list items as completed or uncompleted.",
        ) {
            val idsToCompleteState =
                required<Map<String, Boolean>>(
                    name = "ids_to_complete_state",
                    description = "Map one or more item ids to the new desired completed state",
                )

            handle {
                val group =
                    getActiveMcpGroupUseCase(userId)
                        ?: return@handle toolError("No group is available for this user.")

                val output =
                    changeShoppingListItemsCompleteStateUseCase(
                        ChangeShoppingListItemsCompleteStateUseCase.Input(
                            byUserId = userId,
                            groupId = group.id,
                            idsToCompleteState = idsToCompleteState(),
                        ),
                    )

                success(
                    Output(
                        changedItems = output.changedItems.map { it.toMcpReferenceDto() },
                        alreadyChangedItems = output.alreadyChangedItems.map { it.toMcpReferenceDto() },
                        missingIds = output.missingIds,
                        conflictedIds = output.conflictedIds,
                    ),
                )
            }
        }
    }

    @Serializable
    private data class Output(
        val changedItems: List<ShoppingListItemReferenceMcpDto>,
        val alreadyChangedItems: List<ShoppingListItemReferenceMcpDto>,
        val missingIds: List<String>,
        val conflictedIds: List<String>,
    )
}
