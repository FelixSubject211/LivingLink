package felix.projekt.livinglink.server

import com.auth0.jwk.JwkProviderBuilder
import felix.projekt.livinglink.server.auth.application.DeleteUserDefaultUseCase
import felix.projekt.livinglink.server.auth.application.LoginUserDefaultUseCase
import felix.projekt.livinglink.server.auth.application.LogoutUserDefaultUseCase
import felix.projekt.livinglink.server.auth.application.RefreshUserTokenDefaultUseCase
import felix.projekt.livinglink.server.auth.application.RegisterUserDefaultUseCase
import felix.projekt.livinglink.server.auth.config.AuthConfig
import felix.projekt.livinglink.server.auth.config.authDefaultConfig
import felix.projekt.livinglink.server.auth.infrastructure.KeycloakClient
import felix.projekt.livinglink.server.auth.routes.authRoutes
import felix.projekt.livinglink.server.config.appDefaultConfig
import felix.projekt.livinglink.server.eventSourcing.application.AppendEventDefaultUseCase
import felix.projekt.livinglink.server.eventSourcing.application.PollEventsDefaultUseCase
import felix.projekt.livinglink.server.eventSourcing.config.EventSourcingConfig
import felix.projekt.livinglink.server.eventSourcing.config.eventSourcingDefaultConfig
import felix.projekt.livinglink.server.eventSourcing.infrastructure.EventSourcingPostgresRepository
import felix.projekt.livinglink.server.eventSourcing.routes.eventSourcingRoutes
import felix.projekt.livinglink.server.groups.application.CheckGroupMembershipDefaultService
import felix.projekt.livinglink.server.groups.application.CreateGroupDefaultUseCase
import felix.projekt.livinglink.server.groups.application.CreateInviteCodeDefaultUseCase
import felix.projekt.livinglink.server.groups.application.DeleteInviteCodeDefaultUseCase
import felix.projekt.livinglink.server.groups.application.GetUserGroupsDefaultUseCase
import felix.projekt.livinglink.server.groups.application.JoinGroupWithInviteCodeDefaultUseCase
import felix.projekt.livinglink.server.groups.application.RemoveUserFromGroupsDefaultService
import felix.projekt.livinglink.server.groups.config.GroupsConfig
import felix.projekt.livinglink.server.groups.config.groupsDefaultConfig
import felix.projekt.livinglink.server.groups.infrastructure.GroupMongoDbRepository
import felix.projekt.livinglink.server.groups.infrastructure.GroupVersionRedisCache
import felix.projekt.livinglink.server.groups.routes.groupRoutes
import felix.projekt.livinglink.shared.json
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
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
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.routing.routing
import java.net.URL
import java.util.UUID
import java.util.concurrent.TimeUnit

fun main() {
    val appConfig = appDefaultConfig()
    embeddedServer(
        factory = Netty,
        port = appConfig.serverPort,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module(
    authConfig: AuthConfig = authDefaultConfig(),
    groupsConfig: GroupsConfig = groupsDefaultConfig(),
    eventSourcingConfig: EventSourcingConfig = eventSourcingDefaultConfig()
) {
    install(ContentNegotiation) {
        json(json)
    }

    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.Upgrade)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)
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

    routing {
        val uuidProvider = { UUID.randomUUID().toString() }

        val authClient = KeycloakClient(authConfig)

        val groupMongoDbRepository = GroupMongoDbRepository(
            groupsConfig = groupsConfig,
            uuidProvider = uuidProvider
        )

        val groupVersionCache = GroupVersionRedisCache(
            groupsConfig = groupsConfig
        )

        val removeUserFromGroupsService = RemoveUserFromGroupsDefaultService(
            groupRepository = groupMongoDbRepository,
            groupVersionCache = groupVersionCache
        )

        val checkGroupMembershipService = CheckGroupMembershipDefaultService(
            groupRepository = groupMongoDbRepository,
            groupVersionCache = groupVersionCache
        )

        authRoutes(
            authConfig = authConfig,
            loginUserUseCase = LoginUserDefaultUseCase(
                authClient = authClient
            ),
            registerUserUseCase = RegisterUserDefaultUseCase(
                authClient = authClient
            ),
            refreshUserTokenUseCase = RefreshUserTokenDefaultUseCase(
                authClient = authClient
            ),
            logoutUserUseCase = LogoutUserDefaultUseCase(
                authClient = authClient
            ),
            deleteUserUseCase = DeleteUserDefaultUseCase(
                removeUserFromGroupsService = removeUserFromGroupsService,
                authClient = authClient,
            )
        )

        val eventSourcingRepository = EventSourcingPostgresRepository(
            config = eventSourcingConfig
        )

        val appendEventUseCase = AppendEventDefaultUseCase(
            repository = eventSourcingRepository,
            checkGroupMembershipService = checkGroupMembershipService
        )

        val pollEventsUseCase = PollEventsDefaultUseCase(
            repository = eventSourcingRepository,
            checkGroupMembershipService = checkGroupMembershipService,
            pollPageSize = eventSourcingConfig.pollPageSize
        )

        authenticate(authConfig.authJwtName) {
            groupRoutes(
                groupsConfig = groupsConfig,
                getUserGroupsUseCase = GetUserGroupsDefaultUseCase(
                    groupRepository = groupMongoDbRepository,
                    groupVersionCache = groupVersionCache
                ),
                createGroupUseCase = CreateGroupDefaultUseCase(
                    groupRepository = groupMongoDbRepository,
                    groupVersionCache = groupVersionCache
                ),
                createInviteCodeUseCase = CreateInviteCodeDefaultUseCase(
                    groupRepository = groupMongoDbRepository,
                    groupVersionCache = groupVersionCache,
                    uuidProvider = uuidProvider
                ),
                deleteInviteCodeUseCase = DeleteInviteCodeDefaultUseCase(
                    groupRepository = groupMongoDbRepository,
                    groupVersionCache = groupVersionCache
                ),
                joinGroupWithInviteCodeUseCase = JoinGroupWithInviteCodeDefaultUseCase(
                    groupRepository = groupMongoDbRepository,
                    groupVersionCache = groupVersionCache
                )
            )

            eventSourcingRoutes(
                config = eventSourcingConfig,
                appendEventUseCase = appendEventUseCase,
                pollEventsUseCase = pollEventsUseCase
            )
        }

        monitor.subscribe(ApplicationStopped) {
            groupMongoDbRepository.close()
            eventSourcingRepository.close()
        }
    }
}