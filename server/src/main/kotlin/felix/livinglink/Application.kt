package felix.livinglink

import felix.livinglink.auth.authRoutes
import felix.livinglink.common.AppModule
import felix.livinglink.common.DatabaseInitializer
import felix.livinglink.common.ServerConfig
import felix.livinglink.common.UserPrincipal
import felix.livinglink.change.changeRoutes
import felix.livinglink.common.defaultAppModule
import felix.livinglink.common.defaultServerConfig
import felix.livinglink.groups.groupRoutes
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(
        Netty,
        port = defaultConfig().serverPort,
        host = "0.0.0.0",
        module = Application::module
    )
        .start(wait = true)
}

fun Application.module(
    config: ServerConfig = defaultServerConfig(config = defaultConfig()),
    appModule: AppModule = defaultAppModule(config = config)
) {

    DatabaseInitializer.initialize(appModule.database)

    install(ContentNegotiation) {
        json(Json {
            isLenient = true
        })
    }

    install(Authentication) {
        jwt(config.authenticationConfig) {
            verifier(appModule.jwtService.verifier)

            validate { credential ->
                val userId = credential.payload.getClaim(config.userIdClaim).asString()
                val username = credential.payload.getClaim(config.usernameClaim).asString()
                val sessionId = credential.payload.getClaim(config.sessionIdClaim).asString()
                if (userId != null && sessionId != null) {
                    UserPrincipal(
                        userId = userId,
                        username = username,
                        sessionId = sessionId
                    )
                } else {
                    null
                }
            }
        }
    }

    routing {
        authRoutes(
            config = config,
            authService = appModule.authService
        )

        authenticate(config.authenticationConfig) {
            changeRoutes(
                changeNotifier = appModule.changeNotifier
            )
            groupRoutes(
                groupService = appModule.groupService
            )
        }
    }
}