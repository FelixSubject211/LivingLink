package felix.livinglink.event

import felix.livinglink.Config
import felix.livinglink.auth.AuthModule
import felix.livinglink.common.CommonModule
import felix.livinglink.event.eventbus.DefaultEventBus
import felix.livinglink.event.eventbus.EventBus
import felix.livinglink.event.network.ChangeNotifierDefaultClient

interface EventModule {
    val eventBus: EventBus
}

fun defaultEventModule(
    config: Config,
    commonModule: CommonModule,
    authModule: AuthModule
): EventModule {
    val changeNotifierClient = ChangeNotifierDefaultClient(
        config = config,
        authenticatedHttpClient = authModule.authenticatedHttpClient.client,
        scope = commonModule.defaultScope
    )

    return object : EventModule {
        override val eventBus = DefaultEventBus(
            changeNotifierClient = changeNotifierClient,
            authenticatedHttpClient = authModule.authenticatedHttpClient,
            scope = commonModule.defaultScope
        )
    }
}