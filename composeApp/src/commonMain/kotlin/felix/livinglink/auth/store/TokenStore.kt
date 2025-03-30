package felix.livinglink.auth.store

import io.ktor.client.plugins.auth.providers.BearerTokens

interface TokenStore {
    fun get(): BearerTokens?
    fun set(accessToken: String, refreshToken: String)
    fun clear()
}

expect class TokenDefaultStore() : TokenStore
