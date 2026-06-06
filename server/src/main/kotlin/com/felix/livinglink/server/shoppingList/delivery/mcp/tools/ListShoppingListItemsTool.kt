package com.felix.livinglink.server.shoppingList.delivery.mcp.tools

import com.felix.livinglink.server.core.config.TimezoneSettings
import com.felix.livinglink.server.core.delivery.mcp.dsl.McpToolDsl.tool
import com.felix.livinglink.server.core.delivery.mcp.dsl.success
import com.felix.livinglink.server.core.delivery.mcp.server.McpToolRegistrar
import com.felix.livinglink.server.shoppingList.application.ListShoppingListItemsUseCase
import com.felix.livinglink.server.shoppingList.delivery.mcp.dto.ShoppingListItemDetailMcpDto
import com.felix.livinglink.server.shoppingList.delivery.mcp.dto.ShoppingListItemSortMcpDto
import com.felix.livinglink.server.shoppingList.delivery.mcp.dto.toMcpDetailDto
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItemQuery
import com.felix.livinglink.server.user.application.FindUsersByIdsUseCase
import io.modelcontextprotocol.kotlin.sdk.server.Server
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Single

@Single(binds = [McpToolRegistrar::class])
class ListShoppingListItemsTool(
    private val listShoppingListItemsUseCase: ListShoppingListItemsUseCase,
    private val findUsersByIdsUseCase: FindUsersByIdsUseCase,
    private val timezoneSettings: TimezoneSettings,
) : McpToolRegistrar {
    override fun register(
        server: Server,
        userId: String,
    ) {
        server.tool(
            name = "list_shopping_list_items",
            description = "Lists shopping list items with optional filtering, sorting and limit.",
        ) {
            val completed =
                optional<Boolean>(
                    name = "completed",
                    description = "Optional completion status filter.",
                )

            val limit =
                optionalInt(
                    name = "limit",
                    description = "Maximum number of items to return.",
                    minimum = 1,
                    maximum = 500,
                    default = 100,
                )

            val sort =
                optional<ShoppingListItemSortMcpDto>(
                    name = "sort",
                    description = "Sort order.",
                    default = ShoppingListItemSortMcpDto.CreatedAtDescending,
                )

            handle {
                val items =
                    listShoppingListItemsUseCase(
                        query =
                            ShoppingListItemQuery(
                                completed = completed(),
                                limit = limit(),
                                sort = sort().toDomain(),
                            ),
                    )

                val usersById =
                    findUsersByIdsUseCase(
                        ids = items.flatMap { it.referencedUserIds },
                    )

                success(
                    Output(
                        items =
                            items.map { item ->
                                item.toMcpDetailDto(
                                    usersById = usersById,
                                    timezoneSettings = timezoneSettings,
                                )
                            },
                    ),
                )
            }
        }
    }

    @Serializable
    private data class Output(
        val items: List<ShoppingListItemDetailMcpDto>,
    )
}
