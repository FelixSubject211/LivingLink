package felix.livinglink.auth.store

import io.ktor.client.plugins.auth.providers.BearerTokens

actual class TokenDefaultStore : TokenStore {
    private var tokens: BearerTokens? = null

    override fun get(): BearerTokens? {
        return tokens
    }

    override fun set(accessToken: String, refreshToken: String) {
        tokens = BearerTokens(accessToken, refreshToken)
    }

    override fun clear() {
        tokens = null
    }
}