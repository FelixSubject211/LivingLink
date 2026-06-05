package com.felix.livinglink.server.core.delivery.mcp.dsl

import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

private val mcpJson: Json =
    Json {
        prettyPrint = false
        encodeDefaults = true
        explicitNulls = true
    }

fun <T> success(
    serializer: KSerializer<T>,
    value: T,
): CallToolResult =
    CallToolResult(
        content =
            listOf(
                TextContent(
                    mcpJson.encodeToString(serializer, value),
                ),
            ),
    )

inline fun <reified T> success(value: T): CallToolResult =
    success(
        serializer = serializer<T>(),
        value = value,
    )

fun toolError(message: String): CallToolResult =
    CallToolResult(
        content = listOf(TextContent(message)),
        isError = true,
    )
