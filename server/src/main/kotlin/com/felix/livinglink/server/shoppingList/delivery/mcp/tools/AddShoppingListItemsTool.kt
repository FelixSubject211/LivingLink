package com.felix.livinglink.server.shoppingList.delivery.mcp.tools

import com.felix.livinglink.server.core.delivery.mcp.dsl.McpToolDsl.tool
import com.felix.livinglink.server.core.delivery.mcp.dsl.success
import com.felix.livinglink.server.core.delivery.mcp.dsl.toolError
import com.felix.livinglink.server.core.delivery.mcp.server.McpToolRegistrar
import com.felix.livinglink.server.group.application.GetActiveMcpGroupUseCase
import com.felix.livinglink.server.shoppingList.application.AddShoppingListItemsUseCase
import com.felix.livinglink.server.shoppingList.application.ShoppingListItemFactory
import com.felix.livinglink.server.shoppingList.delivery.mcp.dto.ShoppingListItemReferenceMcpDto
import com.felix.livinglink.server.shoppingList.delivery.mcp.dto.toMcpReferenceDto
import io.modelcontextprotocol.kotlin.sdk.server.Server
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Single

@Single(binds = [McpToolRegistrar::class])
class AddShoppingListItemsTool(
    private val addShoppingListItemsUseCase: AddShoppingListItemsUseCase,
    private val getActiveMcpGroupUseCase: GetActiveMcpGroupUseCase,
    private val shoppingListItemFactory: ShoppingListItemFactory,
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
                val group =
                    getActiveMcpGroupUseCase(userId)
                        ?: return@handle toolError("No group is available for this user.")

                val newItems =
                    shoppingListItemFactory.createNewItems(
                        groupId = group.id,
                        names = names(),
                    )

                val items =
                    addShoppingListItemsUseCase(
                        AddShoppingListItemsUseCase.Input(
                            byUserId = userId,
                            groupId = group.id,
                            items = newItems,
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
