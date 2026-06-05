package com.felix.livinglink.server.core.delivery.mcp.dsl

import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("McpToolDsl")

object McpToolDsl {
    fun Server.tool(
        name: String,
        description: String,
        block: McpToolBuilder.() -> Unit,
    ) {
        val builder =
            McpToolBuilder()
                .apply(block)

        val definition =
            McpToolDefinition(
                name = name,
                description = description,
                parameters = builder.parameters,
                handler = builder.handler,
            )

        addTool(
            name = definition.name,
            description = definition.description,
            inputSchema = definition.schema(),
        ) { request ->
            runCatching {
                val arguments =
                    McpToolArguments(
                        values = request.params.arguments,
                    )

                definition.handler.invoke(arguments)
            }.getOrElse { exception ->
                logger.error("Tool '${definition.name}' failed", exception)
                toolError(exception.toToolErrorMessage())
            }
        }
    }
}

class McpToolDefinition(
    val name: String,
    val description: String,
    val parameters: List<McpToolParameter<*>>,
    val handler: suspend McpToolArguments.() -> CallToolResult,
) {
    fun schema(): ToolSchema =
        McpToolSchemaBuilder.build(parameters)
}
