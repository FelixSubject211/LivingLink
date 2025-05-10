package felix.livinglink.eventSourcing.repository

import felix.livinglink.common.model.LivingLinkResult
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
    fun <T : EventSourcingEvent.Payload> eventsOfTypeFlow(
        groupId: String,
        type: KClass<T>
    ): Flow<List<EventSourcingEvent>>

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
        val localIds = eventSourcingStore
            .all(groupId)
            .map { it.map { e -> e.eventId } }
            .firstOrNull()
            .orEmpty()

        val firstMissingId = findFirstMissingSequentialId(localIds)

        if (firstMissingId != null && firstMissingId <= latestRemoteId) {
            val fromId = firstMissingId - 1
            when (val result = eventSourcingNetworkDataSource.getEvents(groupId, fromId)) {
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

    override fun <T : EventSourcingEvent.Payload> eventsOfTypeFlow(
        groupId: String,
        type: KClass<T>
    ): Flow<List<EventSourcingEvent>> {
        return eventSourcingStore.ofType(groupId, type)
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