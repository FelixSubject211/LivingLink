package felix.projekt.livinglink.composeApp.eventSourcing.application

import felix.projekt.livinglink.composeApp.core.domain.Result
import felix.projekt.livinglink.composeApp.eventSourcing.domain.AppendEventResponse
import felix.projekt.livinglink.composeApp.eventSourcing.domain.EventSourcingRepository
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.Aggregator
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.AppendEventService
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.AppendEventService.FinalResult
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.AppendEventService.OperationResult
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.EventTopic
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.GetAggregateService
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.GetProjectionService
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.Projection
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.Projector
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.TopicSubscription
import kotlinx.coroutines.flow.first

class AppendEventDefaultService(
    private val eventSourcingRepository: EventSourcingRepository,
    private val getProjectionService: GetProjectionService
) : AppendEventService {

    private data class AppendContext<TState>(
        val state: TState?,
        val lastEventId: Long
    )

    private suspend fun <TState, R> retryAppend(
        maxRetries: Int,
        loadContext: suspend () -> AppendContext<TState>,
        subscription: TopicSubscription<*>,
        buildEvent: (TState?) -> OperationResult<R>
    ): FinalResult<R> {
        var context = loadContext()

        repeat(maxRetries) {
            val operation = buildEvent(context.state)

            when (operation) {
                is OperationResult.NoOperation -> {
                    return FinalResult.NoOperation(operation.response)
                }

                is OperationResult.EmitEvent -> {
                    val result = eventSourcingRepository.appendEvent(
                        subscription = subscription,
                        payload = operation.payload,
                        expectedLastEventId = context.lastEventId
                    )

                    when (result) {
                        is Result.Success -> {
                            when (result.data) {
                                is AppendEventResponse.Success -> {
                                    return FinalResult.Success(operation.response)
                                }

                                is AppendEventResponse.NotAuthorized -> {
                                    return FinalResult.NotAuthorized(operation.response)
                                }

                                is AppendEventResponse.VersionMismatch -> {
                                    context = loadContext()
                                }
                            }
                        }

                        is Result.Error -> {
                            return FinalResult.NetworkError(operation.response)
                        }
                    }
                }
            }
        }

        return FinalResult.VersionMismatch()
    }


    override suspend operator fun <TTopic : EventTopic, TState, R> invoke(
        aggregator: Aggregator<TTopic, TState>,
        maxRetries: Int,
        buildEvent: (TState) -> OperationResult<R>
    ): FinalResult<R> {
        val aggregateFlow = eventSourcingRepository.getAggregate(aggregator)

        return retryAppend(
            maxRetries = maxRetries,
            subscription = aggregator.subscription,
            loadContext = {
                val data = aggregateFlow.first {
                    it is GetAggregateService.State.Data<*>
                } as GetAggregateService.State.Data<TState>

                AppendContext(
                    state = data.state,
                    lastEventId = data.lastEventId
                )
            },
            buildEvent = { state ->
                buildEvent(state!!)
            }
        )
    }


    override suspend operator fun <TState, TTopic : EventTopic, R> invoke(
        projector: Projector<TState, TTopic>,
        itemId: String,
        maxRetries: Int,
        buildEvent: (TState?) -> OperationResult<R>
    ): FinalResult<R> {
        val projection = getProjectionService(projector)

        return retryAppend(
            maxRetries = maxRetries,
            subscription = projector.subscription,
            loadContext = {
                val data = projection.item(itemId).first {
                    it is Projection.State.Data
                } as Projection.State.Data

                AppendContext(
                    state = data.state,
                    lastEventId = data.lastEventId
                )
            },
            buildEvent = buildEvent
        )
    }

    override suspend fun <TState, TTopic : EventTopic, R> invoke(
        projector: Projector<TState, TTopic>,
        maxRetries: Int,
        buildEvent: () -> OperationResult<R>
    ): FinalResult<R> {
        val projection = getProjectionService(projector)

        return retryAppend(
            maxRetries = maxRetries,
            subscription = projector.subscription,
            loadContext = {
                val data = projection.status().first {
                    it is Projection.State.Data
                } as Projection.State.Data

                AppendContext(
                    state = data.state,
                    lastEventId = data.lastEventId
                )
            },
            buildEvent = {
                buildEvent()
            }
        )
    }
}
