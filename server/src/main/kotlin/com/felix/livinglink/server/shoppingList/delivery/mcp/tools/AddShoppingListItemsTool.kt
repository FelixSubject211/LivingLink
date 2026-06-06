package com.felix.livinglink.server.shoppingList.delivery.mcp.tools

import com.felix.livinglink.server.core.delivery.mcp.dsl.McpToolDsl.tool
import com.felix.livinglink.server.core.delivery.mcp.dsl.success
import com.felix.livinglink.server.core.delivery.mcp.server.McpToolRegistrar
import com.felix.livinglink.server.shoppingList.application.AddShoppingListItemsUseCase
import com.felix.livinglink.server.shoppingList.delivery.mcp.dto.ShoppingListItemReferenceMcpDto
import com.felix.livinglink.server.shoppingList.delivery.mcp.dto.toMcpReferenceDto
import io.modelcontextprotocol.kotlin.sdk.server.Server
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Single

@Single(binds = [McpToolRegistrar::class])
class AddShoppingListItemsTool(
    private val addShoppingListItemsUseCase: AddShoppingListItemsUseCase,
) : McpToolRegistrar {
    override fun register(
        server: Server,
        userId: String,
    ) {
        server.tool(
            name = "add_shopping_list_items",
            description = "Adds one or more items to the shopping list.",
        ) {
            val names =
                required<List<String>>(
                    name = "names",
                    description = "Names of the items.",
                )

            handle {
                val items =
                    addShoppingListItemsUseCase(
                        AddShoppingListItemsUseCase.Input(
                            byUserId = userId,
                            names = names(),
                        ),
                    )

                success(
                    Output(
                        addedItems = items.map { it.toMcpReferenceDto() },
                    ),
                )
            }
        }
    }

    @Serializable
    private data class Output(
        val addedItems: List<ShoppingListItemReferenceMcpDto>,
    )
}
