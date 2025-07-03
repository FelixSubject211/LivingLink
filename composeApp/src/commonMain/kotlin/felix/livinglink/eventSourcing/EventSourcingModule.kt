package felix.livinglink.eventSourcing

import felix.livinglink.Config
import felix.livinglink.auth.AuthModule
import felix.livinglink.common.CommonModule
import felix.livinglink.event.EventModule
import felix.livinglink.eventSourcing.network.EventSourcingNetworkDefaultDataSource
import felix.livinglink.eventSourcing.repository.EventDefaultSynchronizer
import felix.livinglink.eventSourcing.repository.EventSourcingDefaultRepository
import felix.livinglink.eventSourcing.store.AggregateDefaultStore
import felix.livinglink.eventSourcing.store.EventDefaultStore

interface EventSourcingModule {
    val eventSourcingRepository: EventSourcingDefaultRepository
}

fun defaultEventSourcingModule(
    config: Config,
    commonModule: CommonModule,
    authModule: AuthModule,
    eventModule: EventModule
): EventSourcingModule {
    val eventSourcingNetworkDataSource = EventSourcingNetworkDefaultDataSource(
        authenticatedHttpClient = authModule.authenticatedHttpClient.client
    )

    val eventStore = EventDefaultStore()

    val aggregateStore = AggregateDefaultStore()

    val eventSynchronizer = EventDefaultSynchronizer(
        eventStore = eventStore
    )

    return object : EventSourcingModule {
        override val eventSourcingRepository = EventSourcingDefaultRepository(
            config = config,
            eventSourcingNetworkDataSource = eventSourcingNetworkDataSource,
            eventStore = eventStore,
            aggregateStore = aggregateStore,
            eventSynchronizer = eventSynchronizer,
            eventBus = eventModule.eventBus,
            scope = commonModule.defaultScope
        )
    }
}