package com.felix.livinglink.server.core.delivery.mcp.server

import io.modelcontextprotocol.kotlin.sdk.server.Server

fun interface McpToolRegistrar {
    fun register(
        server: Server,
        userId: String,
    )
}
