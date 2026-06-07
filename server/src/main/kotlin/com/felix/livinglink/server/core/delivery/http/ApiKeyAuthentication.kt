package com.felix.livinglink.server.core.delivery.http

import com.felix.livinglink.server.core.domain.User
import com.felix.livinglink.server.user.config.ApiKeyUserSettings
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.bearer

const val API_KEY_AUTH = "api-key-auth"

data class UserPrincipal(
    val user: User,
)

fun Application.installApiKeyAuth(apiKeyUserSettings: ApiKeyUserSettings) {
    install(Authentication) {
        bearer(API_KEY_AUTH) {
            authenticate { credential ->
                apiKeyUserSettings
                    .userForApiKey(credential.token.trim())
                    ?.let { UserPrincipal(it) }
            }
        }
    }
}
