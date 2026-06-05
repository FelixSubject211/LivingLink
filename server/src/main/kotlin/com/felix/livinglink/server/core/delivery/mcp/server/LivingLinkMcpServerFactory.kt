package com.felix.livinglink.server.core.delivery.mcp.server

import com.felix.livinglink.server.core.config.TimezoneSettings
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import org.koin.core.annotation.Single

@Single(binds = [McpServerFactory::class])
class LivingLinkMcpServerFactory(
    private val toolRegistrars: List<McpToolRegistrar>,
    private val timezoneSettings: TimezoneSettings,
) : McpServerFactory {
    override fun create(userId: String): Server =
        Server(
            serverInfo =
                Implementation(
                    name = "livinglink",
                    version = "0.1.0",
                ),
            options =
                ServerOptions(
                    capabilities =
                        ServerCapabilities(
                            tools = ServerCapabilities.Tools(listChanged = true),
                        ),
                ),
            instructions = buildInstructions(),
        ).also { server ->
            toolRegistrars.forEach { toolRegistrar ->
                toolRegistrar.register(
                    server = server,
                    userId = userId,
                )
            }
        }

    private fun buildInstructions(): String =
        buildString {
            append("All timestamps are in ")
            append(timezoneSettings.timezoneId)
            append(". Display them as-is without conversion.")
        }.trim()
}
