package felix.projekt.livinglink.composeApp

import felix.projekt.livinglink.composeApp.auth.application.DeleteUserDefaultUseCase
import felix.projekt.livinglink.composeApp.auth.application.GetAuthSessionDefaultUseCase
import felix.projekt.livinglink.composeApp.auth.application.GetAuthStateDefaultService
import felix.projekt.livinglink.composeApp.auth.application.LoginUserDefaultUseCase
import felix.projekt.livinglink.composeApp.auth.application.LogoutUserDefaultUseCase
import felix.projekt.livinglink.composeApp.auth.application.RegisterUserDefaultUseCase
import felix.projekt.livinglink.composeApp.auth.infrastructure.AuthNetworkDefaultDataSource
import felix.projekt.livinglink.composeApp.auth.infrastructure.AuthTokenDefaultManager
import felix.projekt.livinglink.composeApp.auth.infrastructure.getTokenPlatformStorage
import felix.projekt.livinglink.composeApp.eventSourcing.application.AppendEventDefaultService
import felix.projekt.livinglink.composeApp.eventSourcing.application.EventSourcingDefaultRepository
import felix.projekt.livinglink.composeApp.eventSourcing.application.EventSynchronizer
import felix.projekt.livinglink.composeApp.eventSourcing.application.GetAggregateDefaultService
import felix.projekt.livinglink.composeApp.eventSourcing.infrastructure.EventSourcingNetworkDefaultDataSource
import felix.projekt.livinglink.composeApp.eventSourcing.infrastructure.InMemoryEventStore
import felix.projekt.livinglink.composeApp.groups.application.CreateGroupDefaultUseCase
import felix.projekt.livinglink.composeApp.groups.application.CreateInviteCodeDefaultUseCase
import felix.projekt.livinglink.composeApp.groups.application.DeleteInviteCodeDefaultUseCase
import felix.projekt.livinglink.composeApp.groups.application.GetGroupDefaultUseCase
import felix.projekt.livinglink.composeApp.groups.application.GetGroupsDefaultUseCase
import felix.projekt.livinglink.composeApp.groups.application.GroupsDefaultRepository
import felix.projekt.livinglink.composeApp.groups.application.JoinGroupWithInviteCodeDefaultUseCase
import felix.projekt.livinglink.composeApp.groups.infrastructure.GroupsNetworkDefaultDataSource
import felix.projekt.livinglink.composeApp.shoppingList.application.CheckShoppingListItemDefaultUseCase
import felix.projekt.livinglink.composeApp.shoppingList.application.CreateShoppingListItemDefaultUseCase
import felix.projekt.livinglink.composeApp.shoppingList.application.GetShoppingListStateDefaultUseCase
import felix.projekt.livinglink.composeApp.shoppingList.application.UncheckShoppingListItemDefaultUseCase
import felix.projekt.livinglink.shared.json
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

object AppModule {
    val scope by lazy {
        CoroutineScope(Dispatchers.Default)
    }

    val authNetworkDataSource by lazy {
        AuthNetworkDefaultDataSource(
            httpClient = HttpClient {
                install(ContentNegotiation) { json(json) }
                defaultRequest {
                    url {
                        protocol = AppConfig.serverUrlProtocol
                        host = AppConfig.serverHost
                        port = AppConfig.serverPort
                    }
                }
            }
        )
    }

    val authTokenManager by lazy {
        AuthTokenDefaultManager(
            scope = scope,
            tokenStorage = getTokenPlatformStorage(),
            authNetworkDataSource = authNetworkDataSource
        )
    }

    val getAuthStateService by lazy {
        GetAuthStateDefaultService(
            authTokenManager = authTokenManager
        )
    }

    val loginUseCase by lazy {
        LoginUserDefaultUseCase(
            authTokenManager = authTokenManager,
            authNetworkDataSource = authNetworkDataSource
        )
    }

    val registerUseCase by lazy {
        RegisterUserDefaultUseCase(
            authTokenManager = authTokenManager,
            authNetworkDataSource = authNetworkDataSource
        )
    }

    val logoutUserUseCase by lazy {
        LogoutUserDefaultUseCase(
            authTokenManager = authTokenManager,
            authNetworkDataSource = authNetworkDataSource
        )
    }

    val deleteUserUseCase by lazy {
        DeleteUserDefaultUseCase(
            authTokenManager = authTokenManager,
            authNetworkDataSource = authNetworkDataSource
        )
    }

    val getAuthSessionUseCase by lazy {
        GetAuthSessionDefaultUseCase(
            authTokenManager = authTokenManager
        )
    }

    val groupsNetworkDataSource by lazy {
        GroupsNetworkDefaultDataSource(
            httpClient = authTokenManager.client
        )
    }

    val groupsRepository by lazy {
        GroupsDefaultRepository(
            groupsNetworkDataSource = groupsNetworkDataSource,
            getAuthStateService = getAuthStateService,
            scope = scope
        )
    }

    val getGroupsUseCase by lazy {
        GetGroupsDefaultUseCase(
            groupsRepository = groupsRepository
        )
    }

    val getGroupUseCase by lazy {
        GetGroupDefaultUseCase(
            groupsRepository = groupsRepository
        )
    }

    val createGroupUseCase by lazy {
        CreateGroupDefaultUseCase(
            groupsRepository = groupsRepository
        )
    }

    val createInviteCodeUseCase by lazy {
        CreateInviteCodeDefaultUseCase(
            groupsRepository = groupsRepository
        )
    }

    val deleteInviteCodeUseCase by lazy {
        DeleteInviteCodeDefaultUseCase(
            groupsRepository = groupsRepository
        )
    }

    val joinGroupWithInviteCodeUseCase by lazy {
        JoinGroupWithInviteCodeDefaultUseCase(
            groupsRepository = groupsRepository
        )
    }

    val eventSourcingNetworkDataSource by lazy {
        EventSourcingNetworkDefaultDataSource(
            httpClient = authTokenManager.client
        )
    }

    val eventStore by lazy {
        InMemoryEventStore()
    }

    val eventSourcingRepository by lazy {
        EventSourcingDefaultRepository(
            eventSynchronizer = EventSynchronizer(
                eventStore = eventStore,
                eventSourcingNetworkDataSource = eventSourcingNetworkDataSource,
                scope = scope
            ),
            eventStore = eventStore,
            getAuthStateService = getAuthStateService,
            scope = scope
        )
    }

    val getAggregateService by lazy {
        GetAggregateDefaultService(
            repository = eventSourcingRepository
        )
    }

    val appendEventService by lazy {
        AppendEventDefaultService(
            eventSourcingRepository = eventSourcingRepository
        )
    }

    val getShoppingListStateUseCase by lazy {
        GetShoppingListStateDefaultUseCase(
            getAggregateService = getAggregateService
        )
    }

    val createShoppingListItemUseCase by lazy {
        CreateShoppingListItemDefaultUseCase(
            appendEventService = appendEventService
        )
    }

    val checkShoppingListItemUseCase by lazy {
        CheckShoppingListItemDefaultUseCase(
            appendEventService = appendEventService
        )
    }

    val uncheckShoppingListItemUseCase by lazy {
        UncheckShoppingListItemDefaultUseCase(
            appendEventService = appendEventService
        )
    }
}