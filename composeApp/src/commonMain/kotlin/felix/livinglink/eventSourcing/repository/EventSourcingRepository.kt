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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

interface EventSourcingRepository {
    fun <T : EventSourcingEvent.Payload> eventsOfTypeFlowTyped(
        groupId: String,
        type: KClass<T>
    ): Flow<RepositoryState<List<EventSourcingEvent>, Nothing>>

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
                        eventSourcingStore.clear()
                    }
                    else -> {}
                }
            }
        }
    }

    private suspend fun handleGroupStateUpdate(groupId: String, latestRemoteId: Long) {
        val localEventIds = eventSourcingStore
            .all(groupId)
            .map { it.map { e -> e.eventId } }
            .firstOrNull()
            .orEmpty()

        val firstMissingId = findFirstMissingSequentialId(localEventIds)

        val syncStartExclusiveId: Long? = when {
            firstMissingId != null && firstMissingId <= latestRemoteId -> {
                firstMissingId - 1
            }

            firstMissingId == null && localEventIds.size.toLong() <= latestRemoteId -> {
                localEventIds.size.toLong() - 1
            }

            else -> null
        }

        if (syncStartExclusiveId != null) {
            when (val result = eventSourcingNetworkDataSource.getEvents(
                groupId = groupId,
                sinceEventIdExclusive = syncStartExclusiveId
            )
            ) {
                is LivingLinkResult.Success -> eventSourcingStore.merge(groupId, result.data.events)
                is LivingLinkResult.Error<*> -> {}
            }
        }
    }

    private fun findFirstMissingSequentialId(ids: List<Long>): Long? {
        for (i in ids.indices) {
            if (ids[i] != i.toLong()) return i.toLong()
        }
        return null
    }

    override fun <T : EventSourcingEvent.Payload> eventsOfTypeFlowTyped(
        groupId: String,
        type: KClass<T>
    ): Flow<RepositoryState<List<EventSourcingEvent>, Nothing>> {
        return eventSourcingStore
            .ofType(groupId, type)
            .map { events ->
                RepositoryState.Data(events)
            }
    }

    override suspend fun addEvent(
        groupId: String,
        payload: EventSourcingEvent.Payload
    ): LivingLinkResult<Unit, NetworkError> {
        return when (val result = eventSourcingNetworkDataSource.appendEvent(
            AppendEventSourcingEventRequest(groupId, payload)
        )) {
            is LivingLinkResult.Success -> {
                eventSourcingStore.merge(groupId, listOf(result.data.event))
                LivingLinkResult.Success(Unit)
            }
            is LivingLinkResult.Error -> result
        }
    }
}