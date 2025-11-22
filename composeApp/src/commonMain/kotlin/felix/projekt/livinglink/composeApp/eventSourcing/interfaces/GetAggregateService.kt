package felix.projekt.livinglink.composeApp.eventSourcing.interfaces

import kotlinx.coroutines.flow.StateFlow

interface GetAggregateService {
    operator fun <TTopic : EventTopic, TState> invoke(
        aggregator: Aggregator<TTopic, TState>
    ): StateFlow<EventAggregateState<TState>>
}