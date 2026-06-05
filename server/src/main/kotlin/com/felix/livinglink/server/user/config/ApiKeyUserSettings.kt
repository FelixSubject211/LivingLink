package com.felix.livinglink.server.user.config

import com.felix.livinglink.server.core.config.Env
import com.felix.livinglink.server.core.domain.User
import org.koin.core.annotation.Single

@Single
class ApiKeyUserSettings {
    private val usersByApiKey: Map<String, User> by lazy {
        Env
            .required("LIVINGLINK_MCP_API_KEYS")
            .split(",")
            .associate { rawEntry ->
                val parts =
                    rawEntry
                        .trim()
                        .split(":", limit = 3)

                require(parts.size == 3) {
                    "Invalid LIVINGLINK_MCP_API_KEYS entry '$rawEntry'. Expected: userId:username:apiKey"
                }

                val userId = parts[0].trim()
                val username = parts[1].trim()
                val apiKey = parts[2].trim()

                require(userId.isNotBlank()) {
                    "Invalid LIVINGLINK_MCP_API_KEYS entry '$rawEntry': userId is blank."
                }
                require(username.isNotBlank()) {
                    "Invalid LIVINGLINK_MCP_API_KEYS entry '$rawEntry': username is blank."
                }
                require(apiKey.isNotBlank()) {
                    "Invalid LIVINGLINK_MCP_API_KEYS entry '$rawEntry': apiKey is blank."
                }

                apiKey to
                    User(
                        id = userId,
                        username = username,
                    )
            }
    }

    val usersById: Map<String, User> by lazy {
        usersByApiKey.values.associateBy { user ->
            user.id
        }
    }

    fun userForApiKey(apiKey: String): User? =
        usersByApiKey[apiKey]
}
