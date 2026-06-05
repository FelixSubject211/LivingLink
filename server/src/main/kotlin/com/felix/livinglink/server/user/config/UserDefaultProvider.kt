package com.felix.livinglink.server.user.config

import com.felix.livinglink.server.core.config.McpTransport
import com.felix.livinglink.server.core.config.McpTransportSettings
import com.felix.livinglink.server.core.domain.User
import com.felix.livinglink.server.user.domain.UserProvider
import org.koin.core.annotation.Single

@Single
class UserDefaultProvider(
    private val transportSettings: McpTransportSettings,
    private val apiKeySettings: ApiKeyUserSettings,
    private val stdioUserSettings: StdioUserSettings,
) : UserProvider {
    override fun usersById(): Map<String, User> =
        when (transportSettings.transport) {
            McpTransport.Stdio -> {
                val user = stdioUserSettings.user
                mapOf(user.id to user)
            }

            McpTransport.Http ->
                apiKeySettings.usersById
        }
}
