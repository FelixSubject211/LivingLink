package felix.livinglink.eventSourcing.repository

import felix.livinglink.common.model.LivingLinkResult
import felix.livinglink.common.model.RepositoryState
import felix.livinglink.common.network.NetworkError
import felix.livinglink.event.eventbus.EventBus
import felix.livinglink.eventSourcing.AppendEventSourcingEventRequest
import felix.livinglink.eventSourcing.EventSourcingEvent
import felix.livinglink.eventSourcing.network.EventSourcingNetworkDataSource
import felix.livinglink.eventSourcing.store.EventSourcingStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

interface EventSourcingRepository {
    fun <T : EventSourcingEvent.Payload, A> aggregateState(
        groupId: String,
        aggregationKey: String,
        type: KClass<T>,
        initial: A,
        reduce: (A, EventSourcingEvent) -> A,
        isEmpty: (A) -> Boolean,
        serializer: KSerializer<A>
    ): Flow<RepositoryState<A, Nothing>>

    suspend fun addEvent(
        groupId: String,
        payload: EventSourcingEvent.Payload
    ): LivingLinkResult<Unit, NetworkError>
}

class EventSourcingDefaultRepository(
    private val eventSourcingNetworkDataSource: EventSourcingNetworkDataSource,
    private val eventSourcingStore: EventSourcingStore,
    private val eventBus: EventBus,
    private val scope: CoroutineScope
) : EventSourcingRepository {

    init {
        observeEventBus()
    }

    private fun observeEventBus() {
        scope.launch {
            eventBus.events.collect { event ->
                when (event) {
                    is EventBus.Event.GroupStateUpdated -> {
                        handleGroupStateUpdate(event.groupId, event.latestEventId)
                    }
                    is EventBus.Event.ClearAll -> {
                        eventSourcingStore.clearAll()
                    }
                    else -> {}
                }
            }
        }
    }

    private suspend fun handleGroupStateUpdate(groupId: String, latestRemoteId: Long?) {
        val expectedLocalId = eventSourcingStore.getNextExpectedEventId(groupId)

        if (latestRemoteId == null || expectedLocalId > latestRemoteId) {
            eventSourcingStore.triggerGroupUpdateCallback(groupId)
            return
        }

        val syncStartExclusiveId = expectedLocalId - 1

        when (val result = eventSourcingNetworkDataSource.getEvents(
            groupId = groupId,
            sinceEventIdExclusive = syncStartExclusiveId
        )) {
            is LivingLinkResult.Success -> {
                eventSourcingStore.appendEvents(
                    groupId = groupId,
                    newEvents = result.data.events
                )
            }

            is LivingLinkResult.Error<*> -> {}
        }
    }

    override fun <T : EventSourcingEvent.Payload, A> aggregateState(
        groupId: String,
        aggregationKey: String,
        type: KClass<T>,
        initial: A,
        reduce: (A, EventSourcingEvent) -> A,
        isEmpty: (A) -> Boolean,
        serializer: KSerializer<A>
    ): Flow<RepositoryState<A, Nothing>> {
        return eventSourcingStore.aggregateState(
            groupId = groupId,
            aggregationKey = aggregationKey,
            type = type,
            initial = initial,
            reduce = reduce,
            isEmpty = isEmpty,
            serializer = serializer
        )
    }

    override suspend fun addEvent(
        groupId: String,
        payload: EventSourcingEvent.Payload
    ): LivingLinkResult<Unit, NetworkError> {
        return when (val result = eventSourcingNetworkDataSource.appendEvent(
            AppendEventSourcingEventRequest(groupId, payload)
        )) {
            is LivingLinkResult.Success -> {
                eventSourcingStore.appendEvents(groupId, listOf(result.data.event))
                LivingLinkResult.Success(Unit)
            }
            is LivingLinkResult.Error -> result
        }
    }
}