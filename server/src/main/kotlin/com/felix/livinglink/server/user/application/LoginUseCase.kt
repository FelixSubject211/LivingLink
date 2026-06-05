package com.felix.livinglink.server.user.application

import com.felix.livinglink.server.user.config.ApiKeyUserSettings
import org.koin.core.annotation.Single

@Single
class LoginUseCase(
    private val apiKeyUserSettings: ApiKeyUserSettings,
) {
    operator fun invoke(apiKey: String): Output {
        val user = apiKeyUserSettings.userForApiKey(apiKey.trim())

        return if (user == null) {
            Output.Invalid
        } else {
            Output.Valid(userId = user.id, username = user.username)
        }
    }

    sealed interface Output {
        data class Valid(
            val userId: String,
            val username: String,
        ) : Output

        data object Invalid : Output
    }
}
