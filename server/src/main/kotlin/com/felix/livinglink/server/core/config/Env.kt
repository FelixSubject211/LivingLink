package com.felix.livinglink.server.core.config

object Env {
    fun required(name: String): String =
        System
            .getenv(name)
            ?.trim()
            ?.takeIf { value ->
                value.isNotBlank()
            }
            ?: error("Missing required environment variable: $name")

    fun requiredInt(name: String): Int =
        required(name).toIntOrNull()
            ?: error("Environment variable '$name' must be an integer.")
}
