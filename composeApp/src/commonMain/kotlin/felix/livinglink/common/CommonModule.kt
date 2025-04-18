package felix.livinglink.common

import felix.livinglink.Config
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json

interface CommonModule {
    val defaultScope: CoroutineScope
    val mainScope: CoroutineScope
    val httpClient: HttpClient
}

fun defaultCommonModule(
    config: Config,
    engine: HttpClientEngine
): CommonModule {
    return object : CommonModule {
        override val defaultScope = CoroutineScope(Dispatchers.Default)

        override val mainScope = CoroutineScope(Dispatchers.Main)

        override val httpClient = HttpClient(engine) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            defaultRequest {
                url {
                    protocol = URLProtocol.HTTP
                    host = config.serverHost
                    port = config.serverPort
                }
            }
        }
    }
}