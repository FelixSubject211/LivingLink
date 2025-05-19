package felix.livinglink.eventSourcing

import felix.livinglink.auth.AuthModule
import felix.livinglink.common.CommonModule
import felix.livinglink.event.EventModule
import felix.livinglink.eventSourcing.network.EventSourcingNetworkDefaultDataSource
import felix.livinglink.eventSourcing.repository.EventSourcingDefaultRepository
import felix.livinglink.eventSourcing.store.EventSourcingDefaultStore

interface EventSourcingModule {
    val eventSourcingRepository: EventSourcingDefaultRepository
}

fun defaultEventSourcingModule(
    commonModule: CommonModule,
    authModule: AuthModule,
    eventModule: EventModule
): EventSourcingModule {
    val eventSourcingNetworkDataSource = EventSourcingNetworkDefaultDataSource(
        authenticatedHttpClient = authModule.authenticatedHttpClient.client
    )

    val eventSourcingStore = EventSourcingDefaultStore()

    return object : EventSourcingModule {
        override val eventSourcingRepository = EventSourcingDefaultRepository(
            eventSourcingNetworkDataSource = eventSourcingNetworkDataSource,
            eventSourcingStore = eventSourcingStore,
            eventBus = eventModule.eventBus,
            scope = commonModule.defaultScope
        )
    }
}