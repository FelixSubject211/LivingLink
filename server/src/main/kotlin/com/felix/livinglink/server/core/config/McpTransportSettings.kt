package com.felix.livinglink.server.core.config

import org.koin.core.annotation.Single

@Single
class McpTransportSettings {
    val transport: McpTransport =
        Env
            .required("LIVINGLINK_MCP_TRANSPORT")
            .lowercase()
            .let(McpTransport::fromValue)

    val httpHost: String by lazy {
        Env.required("LIVINGLINK_MCP_HTTP_HOST")
    }

    val httpPort: Int by lazy {
        Env.requiredInt("LIVINGLINK_MCP_HTTP_PORT")
    }

    val httpPath: String by lazy {
        Env.required("LIVINGLINK_MCP_HTTP_PATH")
    }
}

enum class McpTransport(
    val value: String,
) {
    Stdio("stdio"),
    Http("http"),
    ;

    companion object {
        fun fromValue(value: String): McpTransport =
            entries.firstOrNull { transport ->
                transport.value == value
            } ?: error(
                "Unsupported MCP transport '$value'. Use one of: ${
                    entries.joinToString { transport ->
                        transport.value
                    }
                }.",
            )
    }
}
