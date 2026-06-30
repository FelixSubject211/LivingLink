package com.felix.livinglink.server.shoppingList.delivery.mcp.tools

import com.felix.livinglink.server.core.delivery.mcp.dsl.McpToolDsl.tool
import com.felix.livinglink.server.core.delivery.mcp.dsl.success
import com.felix.livinglink.server.core.delivery.mcp.dsl.toolError
import com.felix.livinglink.server.core.delivery.mcp.server.McpToolRegistrar
import com.felix.livinglink.server.group.application.GetActiveMcpGroupUseCase
import com.felix.livinglink.server.shoppingList.application.MoveShoppingListItemsUseCase
import com.felix.livinglink.server.shoppingList.delivery.mcp.dto.ShoppingListItemReferenceMcpDto
import com.felix.livinglink.server.shoppingList.delivery.mcp.dto.ShoppingListMoveMcpDto
import com.felix.livinglink.server.shoppingList.delivery.mcp.dto.toMcpReferenceDto
import io.modelcontextprotocol.kotlin.sdk.server.Server
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Single

@Single(binds = [McpToolRegistrar::class])
class MoveShoppingListItemsTool(
    private val moveShoppingListItemsUseCase: MoveShoppingListItemsUseCase,
    private val getActiveMcpGroupUseCase: GetActiveMcpGroupUseCase,
) : McpToolRegistrar {
    override fun register(
        server: Server,
        userId: String,
    ) {
        server.tool(
            name = "move_shopping_list_items",
            description =
                "Moves one or more shopping list items. Each move is either " +
                    "type='after' with afterId, placing the item right below afterId, " +
                    "or type='before' with beforeId, placing it right above beforeId. " +
                    "Only the moved items change position. Everything else stays put. " +
                    "Send all moves in a single call. Moves are applied in order, so to " +
                    "stack several items after the same anchor, list them top to bottom. " +
                    "Use item ids from list_shopping_list_items.",
        ) {
            val moves =
                required<List<ShoppingListMoveMcpDto>>(
                    name = "moves",
                    description =
                        "The moves to apply, in order. Each is type='after' with " +
                            "itemId and afterId, or type='before' with itemId and beforeId.",
                )
            handle {
                val group =
                    getActiveMcpGroupUseCase(userId)
                        ?: return@handle toolError("No group is available for this user.")

                val output =
                    moveShoppingListItemsUseCase(
                        MoveShoppingListItemsUseCase.Input(
                            byUserId = userId,
                            groupId = group.id,
                            moves =
                                moves().map { dto ->
                                    when (dto) {
                                        is ShoppingListMoveMcpDto.After ->
                                            MoveShoppingListItemsUseCase.Move.After(
                                                itemId = dto.itemId,
                                                afterId = dto.afterId,
                                            )

                                        is ShoppingListMoveMcpDto.Before ->
                                            MoveShoppingListItemsUseCase.Move.Before(
                                                itemId = dto.itemId,
                                                beforeId = dto.beforeId,
                                            )
                                    }
                                },
                        ),
                    )

                success(
                    Output(
                        movedItems = output.movedItems.map { it.toMcpReferenceDto() },
                        missingIds = output.missingIds,
                        anchorNotFoundIds = output.anchorNotFoundIds,
                        conflictedIds = output.conflictedIds,
                    ),
                )
            }
        }
    }

    @Serializable
    private data class Output(
        val movedItems: List<ShoppingListItemReferenceMcpDto>,
        val missingIds: List<String>,
        val anchorNotFoundIds: List<String>,
        val conflictedIds: List<String>,
    )
}
