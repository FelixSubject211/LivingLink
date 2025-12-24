package felix.projekt.livinglink.composeApp.eventSourcing.application

import felix.projekt.livinglink.composeApp.auth.interfaces.GetAuthStateService
import felix.projekt.livinglink.composeApp.core.Database
import felix.projekt.livinglink.composeApp.core.domain.NetworkError
import felix.projekt.livinglink.composeApp.core.domain.Result
import felix.projekt.livinglink.composeApp.core.domain.withLockNonSuspend
import felix.projekt.livinglink.composeApp.eventSourcing.domain.AppendEventResponse
import felix.projekt.livinglink.composeApp.eventSourcing.domain.EventSourcingRepository
import felix.projekt.livinglink.composeApp.eventSourcing.domain.EventStore
import felix.projekt.livinglink.composeApp.eventSourcing.domain.EventSynchronizer
import felix.projekt.livinglink.composeApp.eventSourcing.infrastructure.SqlDelightProjectionStore
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.Aggregator
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.EventTopic
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.GetAggregateService
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.Projection
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.Projector
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.TopicSubscription
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonElement

class EventSourcingDefaultRepository(
    private val eventSynchronizer: EventSynchronizer,
    private val eventStore: EventStore,
    private val getAuthStateService: GetAuthStateService,
    private val database: Database,
    private val scope: CoroutineScope
) : EventSourcingRepository {
    private val mutex = Mutex()
    private val aggregateManagers = HashMap<AggregateManagerKey, AggregateManager<*, *>>()
    private val projectionManagers = HashMap<ProjectionManagerKey, ProjectionManager<*, *>>()

    init {
        scope.launch {
            getAuthStateService()
                .collect { authState ->
                    when (authState) {
                        GetAuthStateService.AuthState.LoggedOut -> {
                            mutex.withLock {
                                eventSynchronizer.clear()
                                aggregateManagers.values.forEach { manager ->
                                    manager.stop()
                                }
                                aggregateManagers.clear()
                                projectionManagers.values.forEach { manager ->
                                    manager.stop()
                                }
                                projectionManagers.clear()
                                eventStore.clearAll()
                            }
                        }

                        GetAuthStateService.AuthState.LoggedIn -> {}
                    }
                }
        }
    }

    @OptIn(InternalCoroutinesApi::class)
    override fun <TTopic : EventTopic, TState> getAggregate(
        aggregator: Aggregator<TTopic, TState>
    ): StateFlow<GetAggregateService.State<TState>> {
        val key = AggregateManagerKey(
            aggregatorId = aggregator.id,
            subscription = aggregator.subscription
        )

        return mutex.withLockNonSuspend {
            @Suppress("UNCHECKED_CAST")
            aggregateManagers.getOrPut(key) {
                AggregateManager(
                    aggregator = aggregator,
                    updates = eventSynchronizer.subscribe(
                        subscription = aggregator.subscription
                    ),
                    eventStore = eventStore,
                    parentScope = scope
                )
            }.state as StateFlow<GetAggregateService.State<TState>>
        }
    }

    override fun <TState, TTopic : EventTopic> getProjection(
        projector: Projector<TState, TTopic>
    ): Projection<TState> {

        val key = ProjectionManagerKey(
            projectorId = projector.id,
            subscription = projector.subscription
        )

        return mutex.withLockNonSuspend {
            @Suppress("UNCHECKED_CAST")
            projectionManagers.getOrPut(key) {
                val store = SqlDelightProjectionStore(
                    database = database,
                    projectionId = projector.id,
                    subscription = projector.subscription,
                    stateSerializer = projector.stateSerializer
                )

                ProjectionManager(
                    projector = projector,
                    projectionStore = store,
                    updates = eventSynchronizer.subscribe(projector.subscription),
                    eventStore = eventStore,
                    parentScope = scope
                )
            } as Projection<TState>
        }
    }

    override suspend fun appendEvent(
        subscription: TopicSubscription<out EventTopic>,
        payload: JsonElement,
        expectedLastEventId: Long
    ): Result<AppendEventResponse, NetworkError> {
        return mutex.withLock {
            eventSynchronizer.appendEvent(
                subscription = subscription,
                payload = payload,
                expectedLastEventId = expectedLastEventId
            )
        }
    }

    data class AggregateManagerKey(
        val aggregatorId: String,
        val subscription: TopicSubscription<*>
    )

    private data class ProjectionManagerKey(
        val projectorId: String,
        val subscription: TopicSubscription<*>
    )
}