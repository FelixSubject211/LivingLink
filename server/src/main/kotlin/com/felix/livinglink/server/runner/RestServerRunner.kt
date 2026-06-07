package com.felix.livinglink.server.runner

import com.felix.livinglink.server.core.config.HttpTransportSettings
import com.felix.livinglink.server.core.delivery.http.HttpRouteRegistrar
import com.felix.livinglink.server.core.delivery.http.installApiKeyAuth
import com.felix.livinglink.server.user.config.ApiKeyUserSettings
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

@Single
class RestServerRunner(
    private val httpTransportSettings: HttpTransportSettings,
    private val httpRouteRegistrars: List<HttpRouteRegistrar>,
    private val apiKeyUserSettings: ApiKeyUserSettings,
) {
    fun run() {
        embeddedServer(
            factory = CIO,
            host = httpTransportSettings.httpHost,
            port = httpTransportSettings.httpPort,
        ) {
            install(CORS) {
                anyHost()
                allowMethod(HttpMethod.Post)
                allowMethod(HttpMethod.Get)
                allowMethod(HttpMethod.Options)
                allowHeader(HttpHeaders.ContentType)
                allowHeader(HttpHeaders.Authorization)
            }

            install(ContentNegotiation) {
                json(
                    contentType = ContentType.Application.Json,
                    json =
                        Json {
                            ignoreUnknownKeys = true
                            encodeDefaults = true
                            explicitNulls = true
                        },
                )
            }

            installApiKeyAuth(apiKeyUserSettings)

            routing {
                httpRouteRegistrars.forEach { registrar ->
                    registrar.register(this)
                }
            }
        }.start(wait = false)
    }
}
