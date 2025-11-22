package felix.projekt.livinglink.composeApp.eventSourcing.application

import felix.projekt.livinglink.composeApp.core.domain.Result
import felix.projekt.livinglink.composeApp.eventSourcing.domain.AppendEventResponse
import felix.projekt.livinglink.composeApp.eventSourcing.domain.EventSourcingRepository
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.Aggregator
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.AppendEventService
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.EventAggregateState
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.EventTopic
import kotlinx.coroutines.flow.first

class AppendEventDefaultService(
    private val eventSourcingRepository: EventSourcingRepository
) : AppendEventService {

    override suspend operator fun <TTopic : EventTopic, TState, R> invoke(
        aggregator: Aggregator<TTopic, TState>,
        maxRetries: Int,
        buildEvent: (TState) -> AppendEventService.OperationResult<R>
    ): AppendEventService.FinalResult<R> {
        val aggregateFlow = eventSourcingRepository.getAggregate(aggregator)

        var currentState = aggregateFlow.first {
            it is EventAggregateState.Data<*>
        } as EventAggregateState.Data<TState>

        repeat(maxRetries) {
            val operation = buildEvent(currentState.state)

            when (operation) {
                is AppendEventService.OperationResult.NoOperation -> {
                    return AppendEventService.FinalResult.NoOperation(operation.response)
                }

                is AppendEventService.OperationResult.EmitEvent -> {
                    val result = eventSourcingRepository.appendEvent(
                        subscription = aggregator.subscription,
                        payload = operation.payload,
                        expectedLastEventId = currentState.lastEventId
                    )

                    when (result) {
                        is Result.Success -> {
                            when (result.data) {
                                is AppendEventResponse.Success -> {
                                    return AppendEventService.FinalResult.Success(operation.response)
                                }

                                is AppendEventResponse.NotAuthorized -> {
                                    return AppendEventService.FinalResult.NotAuthorized(operation.response)
                                }

                                is AppendEventResponse.VersionMismatch -> {
                                    currentState = aggregateFlow.first { state ->
                                        state is EventAggregateState.Data<*> &&
                                                state.lastEventId != currentState.lastEventId
                                    } as EventAggregateState.Data<TState>
                                }
                            }
                        }

                        is Result.Error -> {
                            return AppendEventService.FinalResult.NetworkError(operation.response)
                        }
                    }
                }
            }
        }

        return AppendEventService.FinalResult.VersionMismatch()
    }
}