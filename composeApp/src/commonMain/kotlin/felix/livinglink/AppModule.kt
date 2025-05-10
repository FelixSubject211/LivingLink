package felix.livinglink

import felix.livinglink.auth.AuthModule
import felix.livinglink.auth.defaultAuthModule
import felix.livinglink.common.CommonModule
import felix.livinglink.common.defaultCommonModule
import felix.livinglink.common.network.createHttpClientEngine
import felix.livinglink.event.defaultEventModule
import felix.livinglink.eventSourcing.defaultEventSourcingModule
import felix.livinglink.groups.GroupsModule
import felix.livinglink.groups.defaultGroupsModule
import felix.livinglink.haptics.HapticsModule
import felix.livinglink.haptics.defaultHapticsModule
import felix.livinglink.ui.UiModule
import felix.livinglink.ui.common.navigation.Navigator
import felix.livinglink.ui.defaultUiModule

interface AppModule {
    val commonModule: CommonModule
    val hapticsModule: HapticsModule
    val authModule: AuthModule
    val groupsModule: GroupsModule
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
    val eventModule = defaultEventModule(
        config = config,
        commonModule = commonModule,
        authModule = authModule
    )
    val groupsModule = defaultGroupsModule(
        commonModule = commonModule,
        authModule = authModule,
        eventModule = eventModule
    )
    val eventSourcingModule = defaultEventSourcingModule(
        commonModule = commonModule,
        authModule = authModule,
        eventModule = eventModule
    )
    val uiModule = defaultUiModule(
        navigator = navigator,
        commonModule = commonModule,
        hapticsModule = hapticsModule,
        authModule = authModule,
        groupsModule = groupsModule
    )

    navigator.addObserver(eventModule.eventBus.currentGroupIdObserver)

    return object : AppModule {
        override val commonModule = commonModule
        override val hapticsModule = hapticsModule
        override val authModule = authModule
        override val groupsModule = groupsModule
        override val uiModule = uiModule
    }
}