package felix.livinglink

import felix.livinglink.auth.AuthModule
import felix.livinglink.auth.defaultAuthModule
import felix.livinglink.common.CommonModule
import felix.livinglink.common.defaultCommonModule
import felix.livinglink.common.network.createHttpClientEngine
import felix.livinglink.haptics.HapticsModule
import felix.livinglink.haptics.defaultHapticsModule
import felix.livinglink.ui.UiModule
import felix.livinglink.ui.common.navigation.Navigator
import felix.livinglink.ui.defaultUiModule

interface AppModule {
    val commonModule: CommonModule
    val hapticsModule: HapticsModule
    val authModule: AuthModule
    val uiModule: UiModule
}

fun defaultAppModule(
    navigator: Navigator,
): AppModule {
    val config = defaultConfig()
    val engine = createHttpClientEngine()

    val commonModule = defaultCommonModule(
        config = config,
        engine = engine
    )
    val hapticsModule = defaultHapticsModule(
        commonModule = commonModule
    )
    val authModule = defaultAuthModule(
        config = config,
        engine = engine,
        commonModule = commonModule
    )
    val uiModule = defaultUiModule(
        navigator = navigator,
        commonModule = commonModule,
        hapticsModule = hapticsModule,
        authModule = authModule
    )

    return object : AppModule {
        override val commonModule = commonModule
        override val hapticsModule = hapticsModule
        override val authModule = authModule
        override val uiModule = uiModule
    }
}