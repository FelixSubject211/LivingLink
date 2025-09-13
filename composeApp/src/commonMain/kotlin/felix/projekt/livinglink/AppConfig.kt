package felix.projekt.livinglink

import io.ktor.http.URLProtocol

@Suppress("MayBeConstant")
object AppConfig {
    val serverUrlProtocol = URLProtocol.HTTP
    val severHost = platformLocalhost
    val serverPort = 8000
}

expect val platformLocalhost: String