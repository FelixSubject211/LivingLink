package felix.livinglink

import dev.mokkery.MockMode
import dev.mokkery.mock
import felix.livinglink.auth.AuthModule
import felix.livinglink.auth.network.AuthNetworkDataSource
import felix.livinglink.auth.network.AuthenticatedHttpClient
import felix.livinglink.auth.network.AuthenticatedHttpDefaultClient
import felix.livinglink.auth.store.TokenStore
import felix.livinglink.common.CommonModule
import felix.livinglink.common.network.createHttpClientEngine
import felix.livinglink.haptics.HapticsController
import felix.livinglink.haptics.HapticsModule
import felix.livinglink.ui.UiModule
import felix.livinglink.ui.common.navigation.Navigator
import felix.livinglink.ui.defaultUiModule
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

interface AppTestModule : UiModule {
    val authenticatedHttpClient: AuthenticatedHttpClient
}

fun defaultAppTestModule(
    config: Config = defaultConfig(),
    navigator: Navigator = mock(mode = MockMode.autofill),
    hapticsController: HapticsController = mock(mode = MockMode.autofill),
    authNetworkDataSource: AuthNetworkDataSource = mock(mode = MockMode.autofill),
    tokenStore: TokenStore = mock(mode = MockMode.autofill)
): AppTestModule {
    val authenticatedHttpClient = AuthenticatedHttpDefaultClient(
        config = config,
        engine = createHttpClientEngine(),
        authNetworkDataSource = authNetworkDataSource,
        tokenStore = tokenStore
    )

    val uiModule = defaultUiModule(
        navigator = navigator,
        commonModule = object : CommonModule {
            override val defaultScope = CoroutineScope(Dispatchers.Default)
            override val mainScope = CoroutineScope(Dispatchers.Default)
            override val httpClient = HttpClient()
        },
        hapticsModule = object : HapticsModule {
            override val hapticsController = hapticsController
        },
        authModule = object : AuthModule {
            override val authenticatedHttpClient = authenticatedHttpClient
        }
    )

    return object : AppTestModule, UiModule by uiModule {
        override val authenticatedHttpClient = authenticatedHttpClient
    }
}