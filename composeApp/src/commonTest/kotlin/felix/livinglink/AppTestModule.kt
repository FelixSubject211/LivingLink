package felix.livinglink

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import felix.livinglink.auth.AuthModule
import felix.livinglink.auth.network.AuthNetworkDataSource
import felix.livinglink.auth.network.AuthenticatedHttpClient
import felix.livinglink.auth.network.AuthenticatedHttpDefaultClient
import felix.livinglink.auth.store.TokenStore
import felix.livinglink.common.CommonModule
import felix.livinglink.common.network.createHttpClientEngine
import felix.livinglink.common.repository.FetchAndStoreDataDefaultHandler
import felix.livinglink.event.eventbus.DefaultEventBus
import felix.livinglink.event.network.ChangeNotifierClient
import felix.livinglink.groups.GroupsModule
import felix.livinglink.groups.network.GroupsNetworkDataSource
import felix.livinglink.groups.repository.GroupsDefaultRepository
import felix.livinglink.groups.store.GroupStore
import felix.livinglink.haptics.HapticsModule
import felix.livinglink.haptics.controller.HapticsController
import felix.livinglink.haptics.store.HapticsSettingsStore
import felix.livinglink.ui.UiModule
import felix.livinglink.ui.common.navigation.Navigator
import felix.livinglink.ui.defaultUiModule
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.test.TestScope

interface AppTestModule : UiModule {
    val authenticatedHttpClient: AuthenticatedHttpClient
}

fun defaultAppTestModule(
    config: Config = defaultConfig(),
    navigator: Navigator = mock(mode = MockMode.autofill),
    hapticsSettingsStore: HapticsSettingsStore = mock(mode = MockMode.autofill) {
        every { updates } returns flowOf<HapticsSettingsStore.Options?>().stateIn(
            scope = TestScope(),
            started = SharingStarted.Eagerly,
            initialValue = null
        )
    },
    hapticsController: HapticsController = mock(mode = MockMode.autofill),
    authNetworkDataSource: AuthNetworkDataSource = mock(mode = MockMode.autofill),
    tokenStore: TokenStore = mock(mode = MockMode.autofill),
    changeNotifierClient: ChangeNotifierClient = mock(mode = MockMode.autofill) {
        every { events } returns emptyFlow()
    },
    groupNetworkDataSource: GroupsNetworkDataSource = mock(mode = MockMode.autofill),
    groupStore: GroupStore = mock(mode = MockMode.autofill) {
        every { groups } returns emptyFlow()
    }
): AppTestModule {
    val authenticatedHttpClient = AuthenticatedHttpDefaultClient(
        config = config,
        engine = createHttpClientEngine(),
        authNetworkDataSource = authNetworkDataSource,
        tokenStore = tokenStore,
        scope = CoroutineScope(Dispatchers.Default)
    )

    val eventBus = DefaultEventBus(
        changeNotifierClient = changeNotifierClient
    )

    val uiModule = defaultUiModule(
        navigator = navigator,
        commonModule = object : CommonModule {
            override val defaultScope = CoroutineScope(Dispatchers.Default)
            override val mainScope = CoroutineScope(Dispatchers.Default)
            override val httpClient = HttpClient()
        },
        hapticsModule = object : HapticsModule {
            override val hapticsSettingsStore = hapticsSettingsStore
            override val hapticsController = hapticsController
        },
        groupsModule = object : GroupsModule {
            override val groupsRepository = GroupsDefaultRepository(
                groupsNetworkDataSource = groupNetworkDataSource,
                groupStore = groupStore,
                eventBus = eventBus,
                fetchAndStoreDataDefaultHandler = FetchAndStoreDataDefaultHandler(
                    scope = CoroutineScope(Dispatchers.Default)
                )
            )
        },
        authModule = object : AuthModule {
            override val authenticatedHttpClient = authenticatedHttpClient
        }
    )

    return object : AppTestModule, UiModule by uiModule {
        override val authenticatedHttpClient = authenticatedHttpClient
    }
}