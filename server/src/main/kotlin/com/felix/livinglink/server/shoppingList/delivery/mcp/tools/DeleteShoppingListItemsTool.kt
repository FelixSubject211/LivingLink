package com.felix.livinglink.server.shoppingList.delivery.mcp.tools

import com.felix.livinglink.server.core.delivery.mcp.dsl.McpToolDsl.tool
import com.felix.livinglink.server.core.delivery.mcp.dsl.success
import com.felix.livinglink.server.core.delivery.mcp.server.McpToolRegistrar
import com.felix.livinglink.server.shoppingList.application.DeleteShoppingListItemsUseCase
import io.modelcontextprotocol.kotlin.sdk.server.Server
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Single

@Single(binds = [McpToolRegistrar::class])
class DeleteShoppingListItemsTool(
    private val deleteShoppingListItemsUseCase: DeleteShoppingListItemsUseCase,
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
                val output =
                    deleteShoppingListItemsUseCase(
                        DeleteShoppingListItemsUseCase.Input(
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
