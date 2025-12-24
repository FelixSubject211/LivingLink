package felix.projekt.livinglink.composeApp.eventSourcing.di

import felix.projekt.livinglink.composeApp.auth.domain.AuthTokenManager
import felix.projekt.livinglink.composeApp.eventSourcing.application.AppendEventDefaultService
import felix.projekt.livinglink.composeApp.eventSourcing.application.EventSourcingDefaultRepository
import felix.projekt.livinglink.composeApp.eventSourcing.application.GetAggregateDefaultService
import felix.projekt.livinglink.composeApp.eventSourcing.application.GetProjectionDefaultService
import felix.projekt.livinglink.composeApp.eventSourcing.application.OnlineEventSynchronizer
import felix.projekt.livinglink.composeApp.eventSourcing.domain.EventSourcingNetworkDataSource
import felix.projekt.livinglink.composeApp.eventSourcing.domain.EventSourcingRepository
import felix.projekt.livinglink.composeApp.eventSourcing.domain.EventStore
import felix.projekt.livinglink.composeApp.eventSourcing.domain.EventSynchronizer
import felix.projekt.livinglink.composeApp.eventSourcing.infrastructure.EventSourcingNetworkDefaultDataSource
import felix.projekt.livinglink.composeApp.eventSourcing.infrastructure.SqlDelightEventStore
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.AppendEventService
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.GetAggregateService
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.GetProjectionService
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
        SqlDelightEventStore(
            database = get()
        )
    }

    single<EventSynchronizer> {
        OnlineEventSynchronizer(
            eventStore = get(),
            eventSourcingNetworkDataSource = get(),
            scope = get()
        )
    }

    single<EventSourcingRepository>(createdAtStart = true) {
        EventSourcingDefaultRepository(
            eventSynchronizer = get(),
            eventStore = get(),
            getAuthStateService = get(),
            database = get(),
            scope = get()
        )
    }

    factoryOf(::GetAggregateDefaultService) bind GetAggregateService::class
    factoryOf(::GetProjectionDefaultService) bind GetProjectionService::class
    factoryOf(::AppendEventDefaultService) bind AppendEventService::class
}