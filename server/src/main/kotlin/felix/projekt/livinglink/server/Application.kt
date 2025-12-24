package felix.projekt.livinglink.server

import com.auth0.jwk.JwkProviderBuilder
import felix.projekt.livinglink.server.auth.config.AuthConfig
import felix.projekt.livinglink.server.auth.di.authModule
import felix.projekt.livinglink.server.auth.interfaces.DeleteUserUseCase
import felix.projekt.livinglink.server.auth.interfaces.LoginUserUseCase
import felix.projekt.livinglink.server.auth.interfaces.LogoutUserUseCase
import felix.projekt.livinglink.server.auth.interfaces.RefreshUserTokenUseCase
import felix.projekt.livinglink.server.auth.interfaces.RegisterUserUseCase
import felix.projekt.livinglink.server.auth.routes.authRoutes
import felix.projekt.livinglink.server.core.config.coreDefaultConfig
import felix.projekt.livinglink.server.core.di.coreModule
import felix.projekt.livinglink.server.eventSourcing.config.EventSourcingConfig
import felix.projekt.livinglink.server.eventSourcing.di.eventSourcingModule
import felix.projekt.livinglink.server.eventSourcing.domain.EventSourcingRepository
import felix.projekt.livinglink.server.eventSourcing.interfaces.AppendEventUseCase
import felix.projekt.livinglink.server.eventSourcing.interfaces.PollEventsUseCase
import felix.projekt.livinglink.server.eventSourcing.routes.eventSourcingRoutes
import felix.projekt.livinglink.server.groups.config.GroupsConfig
import felix.projekt.livinglink.server.groups.di.groupModule
import felix.projekt.livinglink.server.groups.domain.GroupRepository
import felix.projekt.livinglink.server.groups.interfaces.CreateGroupUseCase
import felix.projekt.livinglink.server.groups.interfaces.CreateInviteCodeUseCase
import felix.projekt.livinglink.server.groups.interfaces.DeleteInviteCodeUseCase
import felix.projekt.livinglink.server.groups.interfaces.GetUserGroupsUseCase
import felix.projekt.livinglink.server.groups.interfaces.JoinGroupWithInviteCodeUseCase
import felix.projekt.livinglink.server.groups.routes.groupRoutes
import felix.projekt.livinglink.shared.json
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import org.koin.ktor.ext.get
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import java.net.URL
import java.util.concurrent.TimeUnit

fun main() {
    embeddedServer(
        factory = Netty,
        port = coreDefaultConfig().serverPort,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    val serverModules = listOf(
        coreModule,
        groupModule,
        authModule,
        eventSourcingModule
    )

    install(Koin) {
        slf4jLogger()
        modules(serverModules)
    }

    val authConfig: AuthConfig = get()
    val groupsConfig: GroupsConfig = get()
    val eventSourcingConfig: EventSourcingConfig = get()

    install(ContentNegotiation) {
        json(json)
    }

    val issuer = "http://${authConfig.keycloakHost}:${authConfig.keycloakPort}/realms/${authConfig.keycloakRealm}"

    install(Authentication) {
        jwt(authConfig.authJwtName) {
            realm = authConfig.keycloakRealm

            val jwkProvider = JwkProviderBuilder(
                URL("http://${authConfig.keycloakHost}:${authConfig.keycloakPort}/realms/${authConfig.keycloakRealm}/protocol/openid-connect/certs")
            ).cached(10, 24, TimeUnit.HOURS).build()

            verifier(jwkProvider, issuer) {
                acceptLeeway(30)
            }

            validate { credential ->
                val realmRoles = credential.payload.getClaim("realm_access").asMap()
                    ?.get("roles") as? List<*> ?: emptyList<Any>()

                if (realmRoles.contains(authConfig.keycloakUserRole)) {
                    JWTPrincipal(credential.payload)
                } else null
            }
        }
    }

    val loginUserUseCase: LoginUserUseCase = get()
    val registerUserUseCase: RegisterUserUseCase = get()
    val refreshUserTokenUseCase: RefreshUserTokenUseCase = get()
    val logoutUserUseCase: LogoutUserUseCase = get()
    val deleteUserUseCase: DeleteUserUseCase = get()

    val appendEventUseCase: AppendEventUseCase = get()
    val pollEventsUseCase: PollEventsUseCase = get()

    val groupRepository: GroupRepository = get()
    val eventSourcingRepository: EventSourcingRepository = get()

    val getUserGroupsUseCase: GetUserGroupsUseCase = get()
    val createGroupUseCase: CreateGroupUseCase = get()
    val createInviteCodeUseCase: CreateInviteCodeUseCase = get()
    val deleteInviteCodeUseCase: DeleteInviteCodeUseCase = get()
    val joinGroupWithInviteCodeUseCase: JoinGroupWithInviteCodeUseCase = get()

    routing {
        authRoutes(
            authConfig = authConfig,
            loginUserUseCase = loginUserUseCase,
            registerUserUseCase = registerUserUseCase,
            refreshUserTokenUseCase = refreshUserTokenUseCase,
            logoutUserUseCase = logoutUserUseCase,
            deleteUserUseCase = deleteUserUseCase
        )

        authenticate(authConfig.authJwtName) {

            groupRoutes(
                groupsConfig = groupsConfig,
                getUserGroupsUseCase = getUserGroupsUseCase,
                createGroupUseCase = createGroupUseCase,
                createInviteCodeUseCase = createInviteCodeUseCase,
                deleteInviteCodeUseCase = deleteInviteCodeUseCase,
                joinGroupWithInviteCodeUseCase = joinGroupWithInviteCodeUseCase
            )

            eventSourcingRoutes(
                config = eventSourcingConfig,
                appendEventUseCase = appendEventUseCase,
                pollEventsUseCase = pollEventsUseCase
            )
        }

        monitor.subscribe(ApplicationStopped) {
            groupRepository.close()
            eventSourcingRepository.close()
        }
    }
}
