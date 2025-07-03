package felix.livinglink.eventSourcing.repository

import felix.livinglink.common.model.LivingLinkError
import felix.livinglink.common.model.LivingLinkResult
import felix.livinglink.common.model.RepositoryState
import felix.livinglink.common.model.UnknownError
import felix.livinglink.common.network.NetworkError
import felix.livinglink.event.eventbus.EventBus
import felix.livinglink.eventSourcing.AppendEventSourcingEventRequest
import felix.livinglink.eventSourcing.EventSourcingEvent
import felix.livinglink.eventSourcing.network.EventSourcingNetworkDataSource
import felix.livinglink.eventSourcing.store.AggregateStore
import felix.livinglink.eventSourcing.store.EventStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.reflect.KClass

interface EventSourcingRepository {
    fun <PAYLOAD : EventSourcingEvent.Payload, AGGREGATE : Aggregate<AGGREGATE, PAYLOAD>>
            aggregateState(
        groupId: String,
        aggregationKey: String,
        payloadType: KClass<PAYLOAD>,
        initial: AGGREGATE
    ): Flow<RepositoryState<AGGREGATE, LivingLinkError>>

    suspend fun addEvent(
        groupId: String,
        payload: EventSourcingEvent.Payload
    ): LivingLinkResult<Unit, NetworkError>
}

class EventSourcingDefaultRepository(
    private val eventSourcingNetworkDataSource: EventSourcingNetworkDataSource,
    private val eventStore: EventStore,
    private val aggregateStore: AggregateStore,
    private val eventSynchronizer: EventSynchronizer,
    private val eventBus: EventBus,
    private val scope: CoroutineScope,
) : EventSourcingRepository {

    private val mutex = Mutex()
    private val aggregateManagers = HashMap<CacheKey, AggregateManager<*, *>>()
    private val error = MutableStateFlow<LivingLinkError?>(value = null)

    init {
        observeEventBus()
    }

    private fun observeEventBus() {
        scope.launch {
            eventBus.events.collect { event ->
                when (event) {
                    is EventBus.Event.GroupStateUpdated -> {
                        handleGroupStateUpdated(
                            groupId = event.groupId,
                            latestRemoteId = event.latestEventId
                        )
                    }

                    is EventBus.Event.AttemptedButOffline -> {
                        event.groupId?.let {
                            eventSynchronizer.emitNullUpdate(it)
                        }
                    }

                    is EventBus.Event.ClearAll -> {
                        clearAll()
                    }

                    else -> {}
                }
            }
        }
    }

    private suspend fun handleGroupStateUpdated(
        groupId: String,
        latestRemoteId: Long?
    ) = mutex.withLock {
        val expectedLocalId = eventStore.getNextExpectedEventId(groupId)
        if (latestRemoteId == null || latestRemoteId <= expectedLocalId - 1) {
            eventSynchronizer.emitNullUpdate(groupId)
            return@withLock
        }

        val syncStartExclusiveId = expectedLocalId - 1
        when (
            val result = eventSourcingNetworkDataSource.getEvents(
                groupId,
                sinceEventIdExclusive = syncStartExclusiveId
            )
        ) {
            is LivingLinkResult.Success -> {
                eventSynchronizer.storeAndBroadcastEvents(
                    groupId = groupId,
                    events = result.data.events
                )
            }

            is LivingLinkResult.Error -> Unit
        }
    }

    private suspend fun clearAll() = mutex.withLock {
        eventSynchronizer.clearAll()
        aggregateManagers.values.forEach { it.clear() }
        aggregateManagers.clear()
        eventStore.clearAll()
        aggregateStore.clearAll()
    }

    override fun <PAYLOAD : EventSourcingEvent.Payload, AGGREGATE : Aggregate<AGGREGATE, PAYLOAD>>
            aggregateState(
        groupId: String,
        aggregationKey: String,
        payloadType: KClass<PAYLOAD>,
        initial: AGGREGATE
    ): Flow<RepositoryState<AGGREGATE, LivingLinkError>> {
        val key = CacheKey(
            groupId = groupId,
            aggregationKey = aggregationKey,
            qualifiedTypeName = payloadType.qualifiedName!!
        )

        @Suppress("UNCHECKED_CAST")
        val manager = aggregateManagers.getOrPut(key) {
            AggregateManager(
                groupId = groupId,
                aggregationKey = aggregationKey,
                payloadType = payloadType,
                initial = initial,
                eventStore = eventStore,
                aggregateStore = aggregateStore,
                eventBus = eventBus,
                incomingEvents = eventSynchronizer.eventsFlow(groupId),
                scope = CoroutineScope(context = scope.coroutineContext + SupervisorJob())
            )
        } as AggregateManager<PAYLOAD, AGGREGATE>

        return combine(manager.output, error) { output, error ->
            if (error != null) {
                RepositoryState.Error(error)
            } else {
                output
            }
        }.also { this.error.value = null }
    }

    override suspend fun addEvent(
        groupId: String,
        payload: EventSourcingEvent.Payload
    ): LivingLinkResult<Unit, NetworkError> {
        return when (
            val result = eventSourcingNetworkDataSource.appendEvent(
                AppendEventSourcingEventRequest(groupId, payload)
            )
        ) {
            is LivingLinkResult.Success -> {
                mutex.withLock {
                    try {
                        eventSynchronizer.storeEventsAndPublish(groupId, listOf(result.data.event))
                    } catch (e: Exception) {
                        error.value = UnknownError(e)
                    }
                    LivingLinkResult.Success(Unit)
                }
            }

            is LivingLinkResult.Error -> {
                error.value = result.error
                result
            }
        }
    }
}