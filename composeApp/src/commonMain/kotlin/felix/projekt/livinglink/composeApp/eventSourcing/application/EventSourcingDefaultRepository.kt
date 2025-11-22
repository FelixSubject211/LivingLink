package felix.projekt.livinglink.composeApp.eventSourcing.application

import felix.projekt.livinglink.composeApp.auth.interfaces.GetAuthStateService
import felix.projekt.livinglink.composeApp.core.domain.NetworkError
import felix.projekt.livinglink.composeApp.core.domain.Result
import felix.projekt.livinglink.composeApp.eventSourcing.domain.AppendEventResponse
import felix.projekt.livinglink.composeApp.eventSourcing.domain.EventSourcingRepository
import felix.projekt.livinglink.composeApp.eventSourcing.domain.EventStore
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.Aggregator
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.EventAggregateState
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.EventTopic
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.TopicSubscription
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement

class EventSourcingDefaultRepository(
    private val eventSynchronizer: EventSynchronizer,
    private val eventStore: EventStore,
    private val getAuthStateService: GetAuthStateService,
    private val scope: CoroutineScope
) : EventSourcingRepository {
    private val aggregateManagers = HashMap<AggregateManagerKey, AggregateManager<*, *>>()

    init {
        scope.launch {
            getAuthStateService()
                .drop(1)
                .collect { authState ->
                    when (authState) {
                        GetAuthStateService.AuthState.LoggedOut -> {
                            eventSynchronizer.clear()
                            aggregateManagers.values.forEach { manager ->
                                manager.stop()
                            }
                            aggregateManagers.clear()
                            eventStore.clearAll()
                        }

                        GetAuthStateService.AuthState.LoggedIn -> {}
                    }
                }
        }
    }

    @OptIn(InternalCoroutinesApi::class)
    override fun <TTopic : EventTopic, TState> getAggregate(
        aggregator: Aggregator<TTopic, TState>
    ): StateFlow<EventAggregateState<TState>> {
        val key = AggregateManagerKey(
            aggregatorId = aggregator.id,
            subscription = aggregator.subscription
        )

        @Suppress("UNCHECKED_CAST")
        return aggregateManagers.getOrPut(key) {
            AggregateManager(
                aggregator = aggregator,
                updates = eventSynchronizer.subscribe(
                    subscription = aggregator.subscription
                ),
                eventStore = eventStore,
                parentScope = scope
            )
        }.state as StateFlow<EventAggregateState<TState>>
    }

    override suspend fun appendEvent(
        subscription: TopicSubscription<out EventTopic>,
        payload: JsonElement,
        expectedLastEventId: Long
    ): Result<AppendEventResponse, NetworkError> {
        return eventSynchronizer.appendEvent(
            subscription = subscription,
            payload = payload,
            expectedLastEventId = expectedLastEventId
        )
    }

    data class AggregateManagerKey(
        val aggregatorId: String,
        val subscription: TopicSubscription<*>
    )
}