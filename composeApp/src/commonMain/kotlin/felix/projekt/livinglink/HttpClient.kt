package felix.projekt.livinglink

import io.ktor.client.HttpClient
import io.ktor.client.plugins.defaultRequest

val client: HttpClient by lazy {
    HttpClient {
        defaultRequest {
            url {
                protocol = AppConfig.serverUrlProtocol
                host = AppConfig.severHost
                port = AppConfig.serverPort
            }
        }
    }
}