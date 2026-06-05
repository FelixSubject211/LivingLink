package com.felix.livinglink.server.core.delivery.mcp.server

import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import org.koin.core.annotation.Single

@Single(binds = [StdioMcpTransportFactory::class])
class SystemStdioMcpTransportFactory : StdioMcpTransportFactory {
    override fun create(): StdioServerTransport =
        StdioServerTransport(
            inputStream = System.`in`.asSource().buffered(),
            outputStream = System.out.asSink().buffered(),
        )
}
