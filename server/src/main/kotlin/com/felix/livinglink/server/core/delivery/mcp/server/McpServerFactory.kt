package com.felix.livinglink.server.core.delivery.mcp.server

import io.modelcontextprotocol.kotlin.sdk.server.Server

interface McpServerFactory {
    fun create(userId: String): Server
}
