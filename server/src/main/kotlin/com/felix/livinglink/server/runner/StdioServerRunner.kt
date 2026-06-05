package com.felix.livinglink.server.runner

import com.felix.livinglink.server.core.delivery.mcp.server.McpServerFactory
import com.felix.livinglink.server.core.delivery.mcp.server.StdioMcpTransportFactory
import com.felix.livinglink.server.user.config.StdioUserSettings
import kotlinx.coroutines.Job
import org.koin.core.annotation.Single

@Single
class StdioServerRunner(
    private val mcpServerFactory: McpServerFactory,
    private val stdioMcpTransportFactory: StdioMcpTransportFactory,
    private val stdioUserSettings: StdioUserSettings,
) {
    suspend fun run() {
        val server = mcpServerFactory.create(userId = stdioUserSettings.user.id)
        val transport = stdioMcpTransportFactory.create()
        val keepAlive = Job()

        server.createSession(transport)
        server.onClose { keepAlive.complete() }
        keepAlive.join()
    }
}
