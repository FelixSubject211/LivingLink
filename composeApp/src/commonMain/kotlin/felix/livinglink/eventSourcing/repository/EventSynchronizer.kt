package felix.livinglink.eventSourcing.repository

import felix.livinglink.eventSourcing.EventSourcingEvent
import felix.livinglink.eventSourcing.UserAnonymized
import felix.livinglink.eventSourcing.filterByPayloadType
import felix.livinglink.eventSourcing.store.EventStore
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface EventSynchronizer {
    fun eventsFlow(groupId: String): MutableSharedFlow<List<EventSourcingEvent<*>>?>
    suspend fun emitNullUpdate(groupId: String)
    suspend fun storeAndBroadcastEvents(groupId: String, events: List<EventSourcingEvent<*>>)
    suspend fun storeEventsAndPublish(groupId: String, newEvents: List<EventSourcingEvent<*>>)
    suspend fun clearAll()
}

class EventDefaultSynchronizer(
    private val eventStore: EventStore
) : EventSynchronizer {
    private val mutex = Mutex()
    private val eventFlows = HashMap<String, MutableSharedFlow<List<EventSourcingEvent<*>>?>>()

    override fun eventsFlow(groupId: String) = eventFlows.getOrPut(groupId) {
        MutableSharedFlow(
            replay = 0,
            extraBufferCapacity = Int.MAX_VALUE,
            onBufferOverflow = BufferOverflow.SUSPEND
        )
    }

    override suspend fun emitNullUpdate(groupId: String) {
        eventsFlow(groupId).emit(value = null)
    }

    override suspend fun storeAndBroadcastEvents(
        groupId: String,
        events: List<EventSourcingEvent<*>>
    ) {
        storeEventsAndPublish(groupId, events)
        applyUserAnonymizations(groupId, events)
    }

    override suspend fun storeEventsAndPublish(
        groupId: String,
        newEvents: List<EventSourcingEvent<*>>
    ) = mutex.withLock {
        if (newEvents.isEmpty()) return
        val expectedNextId = eventStore.getNextExpectedEventId(groupId)
        val offset = newEvents.first().eventId - expectedNextId
        require(offset <= 0) {
            "Expected eventId=$expectedNextId, got ${newEvents.first().eventId}"
        }

        val onlyNew = newEvents.drop(-offset.toInt())
        if (onlyNew.isEmpty()) return

        eventStore.storeEvents(groupId, onlyNew)
        eventsFlow(groupId).emit(onlyNew)
    }

    override suspend fun clearAll() = mutex.withLock {
        eventFlows.clear()
    }

    private suspend fun applyUserAnonymizations(
        groupId: String,
        events: List<EventSourcingEvent<*>>
    ) = mutex.withLock {
        events.filterByPayloadType(UserAnonymized::class).forEach {
            eventStore.anonymizeUserIdsIndividually(groupId, it.payload.originalUserId)
        }
    }
}