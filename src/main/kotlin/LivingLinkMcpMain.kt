package com.felix

import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.coroutines.Job
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.util.UUID

data class ShoppingItem(
    val id: String,
    val name: String,
    val completed: Boolean
)

suspend fun main() {
    System.setProperty("kotlin.logging.internal.platform.kotlinLoggingStartupMessageEnabled", "false")

    val shoppingItems = mutableListOf<ShoppingItem>()

    val server = Server(
        serverInfo = Implementation(
            name = "livinglink",
            version = "0.1.0"
        ),
        options = ServerOptions(
            capabilities = ServerCapabilities(
                tools = ServerCapabilities.Tools(listChanged = true)
            )
        )
    )

    server.addTool(
        name = "add_shopping_item",
        description = "Adds an item to the household shopping list.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                put("name", buildJsonObject {
                    put("type", "string")
                    put("description", "Name of the item.")
                })
            },
            required = listOf("name")
        )
    ) { request ->
        val name = request.params.arguments?.get("name")?.jsonPrimitive?.content?.trim()

        if (name.isNullOrBlank()) {
            CallToolResult(
                content = listOf(TextContent("Missing required argument: name")),
                isError = true
            )
        } else {
            val item = ShoppingItem(
                id = UUID.randomUUID().toString(),
                name = name,
                completed = false
            )

            shoppingItems.add(item)

            CallToolResult(
                content = listOf(
                    TextContent("Added shopping item '${item.name}' with id '${item.id}'.")
                )
            )
        }
    }

    server.addTool(
        name = "complete_shopping_item",
        description = "Marks a shopping item as completed.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                put("id", buildJsonObject {
                    put("type", "string")
                    put("description", "ID of the item to complete.")
                })
            },
            required = listOf("id")
        )
    ) { request ->
        val id = request.params.arguments?.get("id")?.jsonPrimitive?.content?.trim()

        if (id.isNullOrBlank()) {
            CallToolResult(
                content = listOf(TextContent("Missing required argument: id")),
                isError = true
            )
        } else {
            val index = shoppingItems.indexOfFirst { it.id == id }

            if (index == -1) {
                CallToolResult(
                    content = listOf(TextContent("Shopping item with id '$id' not found.")),
                    isError = true
                )
            } else {
                val updatedItem = shoppingItems[index].copy(completed = true)
                shoppingItems[index] = updatedItem

                CallToolResult(
                    content = listOf(
                        TextContent("Completed shopping item '${updatedItem.name}' with id '${updatedItem.id}'.")
                    )
                )
            }
        }
    }

    server.addTool(
        name = "list_shopping_items",
        description = "Lists all shopping items.",
        inputSchema = ToolSchema()
    ) {
        if (shoppingItems.isEmpty()) {
            CallToolResult(
                content = listOf(TextContent("Shopping list is empty."))
            )
        } else {
            val text = shoppingItems.joinToString(separator = "\n") { item ->
                val status = if (item.completed) "done" else "open"
                "- [${status}] ${item.name} (id: ${item.id})"
            }

            CallToolResult(
                content = listOf(TextContent(text))
            )
        }
    }

    val transport = StdioServerTransport(
        inputStream = System.`in`.asSource().buffered(),
        outputStream = System.out.asSink().buffered()
    )

    server.createSession(transport)

    val keepAlive = Job()
    server.onClose {
        keepAlive.complete()
    }
    keepAlive.join()
}