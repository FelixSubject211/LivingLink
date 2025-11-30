package felix.projekt.livinglink.composeApp.eventSourcing.di

import felix.projekt.livinglink.composeApp.auth.domain.AuthTokenManager
import felix.projekt.livinglink.composeApp.eventSourcing.application.AppendEventDefaultService
import felix.projekt.livinglink.composeApp.eventSourcing.application.EventSourcingDefaultRepository
import felix.projekt.livinglink.composeApp.eventSourcing.application.EventSynchronizer
import felix.projekt.livinglink.composeApp.eventSourcing.application.GetAggregateDefaultService
import felix.projekt.livinglink.composeApp.eventSourcing.domain.EventSourcingNetworkDataSource
import felix.projekt.livinglink.composeApp.eventSourcing.domain.EventSourcingRepository
import felix.projekt.livinglink.composeApp.eventSourcing.domain.EventStore
import felix.projekt.livinglink.composeApp.eventSourcing.infrastructure.EventDatabaseDriverFactory
import felix.projekt.livinglink.composeApp.eventSourcing.infrastructure.EventDatabaseFactory
import felix.projekt.livinglink.composeApp.eventSourcing.infrastructure.EventSourcingNetworkDefaultDataSource
import felix.projekt.livinglink.composeApp.eventSourcing.infrastructure.InMemoryEventStore
import felix.projekt.livinglink.composeApp.eventSourcing.infrastructure.SqlDelightEventStore
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.AppendEventService
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.GetAggregateService
import io.ktor.util.PlatformUtils
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val eventSourcingModule = module {
    single<EventSourcingNetworkDataSource> {
        EventSourcingNetworkDefaultDataSource(
            httpClient = get<AuthTokenManager>().client
        )
    }

    single<EventStore> {
        if (PlatformUtils.IS_BROWSER) {
            InMemoryEventStore()
        } else {
            SqlDelightEventStore(
                database = EventDatabaseFactory(
                    driverFactory = EventDatabaseDriverFactory()
                ).createDatabase()
            )
        }
    }

    single<EventSourcingRepository> {
        EventSourcingDefaultRepository(
            eventSynchronizer = EventSynchronizer(
                eventStore = get(),
                eventSourcingNetworkDataSource = get(),
                scope = get()
            ),
            eventStore = get(),
            getAuthStateService = get(),
            scope = get()
        )
    }

    factoryOf(::GetAggregateDefaultService) bind GetAggregateService::class
    factoryOf(::AppendEventDefaultService) bind AppendEventService::class
}