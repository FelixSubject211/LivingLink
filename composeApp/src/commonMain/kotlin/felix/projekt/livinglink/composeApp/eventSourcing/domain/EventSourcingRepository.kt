package felix.projekt.livinglink.composeApp.eventSourcing.domain

import felix.projekt.livinglink.composeApp.core.domain.NetworkError
import felix.projekt.livinglink.composeApp.core.domain.Result
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.Aggregator
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.EventTopic
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.GetAggregateService
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.Projection
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.Projector
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.TopicSubscription
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.JsonElement

interface EventSourcingRepository {
    fun <TTopic : EventTopic, TState> getAggregate(
        aggregator: Aggregator<TTopic, TState>
    ): StateFlow<GetAggregateService.State<TState>>

    fun <TState, TTopic : EventTopic> getProjection(
        projector: Projector<TState, TTopic>
    ): Projection<TState>

    suspend fun appendEvent(
        subscription: TopicSubscription<out EventTopic>,
        payload: JsonElement,
        expectedLastEventId: Long
    ): Result<AppendEventResponse, NetworkError>
}