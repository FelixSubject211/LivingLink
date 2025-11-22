package felix.projekt.livinglink.composeApp

import io.ktor.http.URLProtocol


object AppConfig {
    val serverUrlProtocol = URLProtocol.HTTP
    val serverHost = platformLocalhost
    val serverPort = 8080
    val groupsPollFallbackMills = 5000L
    val eventSourcingPollFallbackMills = 5000L
}

expect val platformLocalhost: String