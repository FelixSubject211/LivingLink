package com.felix.livinglink.server.runner

import com.felix.livinglink.server.core.config.McpTransport
import com.felix.livinglink.server.core.config.McpTransportSettings
import com.felix.livinglink.server.core.infrastructure.mongo.MongoClientProvider
import kotlinx.coroutines.coroutineScope
import org.koin.core.annotation.Single

@Single
class ServerRunner(
    private val mcpTransportSettings: McpTransportSettings,
    private val mongoClientProvider: MongoClientProvider,
    private val stdioServerRunner: StdioServerRunner,
    private val restServerRunner: RestServerRunner,
    private val mcpServerRunner: McpServerRunner,
) {
    suspend fun run() {
        try {
            when (mcpTransportSettings.transport) {
                McpTransport.Stdio -> stdioServerRunner.run()
                McpTransport.Http -> {
                    coroutineScope {
                        restServerRunner.run()
                        mcpServerRunner.run()
                    }
                }
            }
        } finally {
            mongoClientProvider.close()
        }
    }
}
