package com.felix.livinglink.server.runner

import com.felix.livinglink.server.core.config.McpTransportSettings
import com.felix.livinglink.server.core.delivery.mcp.server.McpServerFactory
import com.felix.livinglink.server.user.config.ApiKeyUserSettings
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.routing.routing
import io.modelcontextprotocol.kotlin.sdk.server.mcpStreamableHttp
import org.koin.core.annotation.Single
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("McpServerRunner")

@Single
class McpServerRunner(
    private val mcpServerFactory: McpServerFactory,
    private val mcpTransportSettings: McpTransportSettings,
    private val apiKeySettings: ApiKeyUserSettings,
) {
    fun run() {
        embeddedServer(
            factory = CIO,
            host = mcpTransportSettings.httpHost,
            port = mcpTransportSettings.httpPort,
        ) {
            routing {
                mcpStreamableHttp(
                    path = mcpTransportSettings.httpPath,
                ) {
                    val apiKey = call.request.queryParameters["key"]
                    val user = apiKey?.let { apiKeySettings.userForApiKey(it) }

                    if (user == null) {
                        logger.warn("Rejected MCP request: missing or invalid API key.")
                    }

                    requireNotNull(user) { "Missing or invalid API key." }

                    mcpServerFactory.create(userId = user.id)
                }
            }
        }.start(wait = true)
    }
}
