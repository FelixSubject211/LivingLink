package com.felix.livinglink.server.core.delivery.mcp.dsl

fun Throwable.toToolErrorMessage(): String =
    when (this) {
        is IllegalArgumentException -> message ?: "Invalid tool input."
        else -> "Tool execution failed."
    }
