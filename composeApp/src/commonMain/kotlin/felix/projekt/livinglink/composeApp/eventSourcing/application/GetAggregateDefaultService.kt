package felix.projekt.livinglink.composeApp.eventSourcing.application

import felix.projekt.livinglink.composeApp.eventSourcing.domain.EventSourcingRepository
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.Aggregator
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.EventTopic
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.GetAggregateService
import kotlinx.coroutines.flow.StateFlow

class GetAggregateDefaultService(
    private val repository: EventSourcingRepository
) : GetAggregateService {
    override operator fun <TTopic : EventTopic, TState> invoke(
        aggregator: Aggregator<TTopic, TState>
    ): StateFlow<GetAggregateService.State<TState>> {
        return repository.getAggregate(aggregator)
    }
}