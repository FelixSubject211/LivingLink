package felix.projekt.livinglink.server.eventSourcing.di

import felix.projekt.livinglink.server.eventSourcing.application.AnonymizeUserEventsDefaultService
import felix.projekt.livinglink.server.eventSourcing.application.AppendEventDefaultUseCase
import felix.projekt.livinglink.server.eventSourcing.application.DeleteEventsDefaultService
import felix.projekt.livinglink.server.eventSourcing.application.PollEventsDefaultUseCase
import felix.projekt.livinglink.server.eventSourcing.config.EventSourcingConfig
import felix.projekt.livinglink.server.eventSourcing.config.eventSourcingDefaultConfig
import felix.projekt.livinglink.server.eventSourcing.domain.EventSourcingRepository
import felix.projekt.livinglink.server.eventSourcing.infrastructure.EventSourcingPostgresRepository
import felix.projekt.livinglink.server.eventSourcing.interfaces.AnonymizeUserEventsService
import felix.projekt.livinglink.server.eventSourcing.interfaces.AppendEventUseCase
import felix.projekt.livinglink.server.eventSourcing.interfaces.DeleteEventsService
import felix.projekt.livinglink.server.eventSourcing.interfaces.PollEventsUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val eventSourcingModule = module {
    single<EventSourcingConfig> { eventSourcingDefaultConfig() }

    single<EventSourcingRepository> {
        EventSourcingPostgresRepository(
            config = get()
        )
    }

    factoryOf(::AppendEventDefaultUseCase) bind AppendEventUseCase::class
    factoryOf(::PollEventsDefaultUseCase) bind PollEventsUseCase::class
    factoryOf(::DeleteEventsDefaultService) bind DeleteEventsService::class
    factoryOf(::AnonymizeUserEventsDefaultService) bind AnonymizeUserEventsService::class
}