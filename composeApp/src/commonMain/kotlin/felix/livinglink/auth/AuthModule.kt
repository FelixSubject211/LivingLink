package felix.livinglink.auth

import felix.livinglink.Config
import felix.livinglink.auth.network.AuthNetworkDefaultDataSource
import felix.livinglink.auth.network.AuthenticatedHttpClient
import felix.livinglink.auth.network.AuthenticatedHttpDefaultClient
import felix.livinglink.auth.store.TokenDefaultStore
import felix.livinglink.common.CommonModule
import io.ktor.client.engine.HttpClientEngine

interface AuthModule {
    val authenticatedHttpClient: AuthenticatedHttpClient
}

fun defaultAuthModule(
    config: Config,
    engine: HttpClientEngine,
    commonModule: CommonModule
): AuthModule {
    return object : AuthModule {
        private val authNetworkDataSource = AuthNetworkDefaultDataSource(
            httpClient = commonModule.httpClient
        )

        private val tokenStore = TokenDefaultStore()

        override val authenticatedHttpClient = AuthenticatedHttpDefaultClient(
            config = config,
            engine = engine,
            tokenStore = tokenStore,
            authNetworkDataSource = authNetworkDataSource,
            scope = commonModule.defaultScope
        )
    }
}