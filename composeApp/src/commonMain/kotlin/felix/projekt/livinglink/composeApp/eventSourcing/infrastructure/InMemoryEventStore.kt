package felix.projekt.livinglink.composeApp.eventSourcing.infrastructure

import felix.projekt.livinglink.composeApp.eventSourcing.domain.EventStore
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.EventSourcingEvent
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.TopicSubscription
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryEventStore : EventStore {

    private val mutex = Mutex()

    private val eventLog = HashMap<SubscriptionKey, MutableList<EventSourcingEvent>>()

    override suspend fun append(
        subscription: TopicSubscription<*>,
        events: List<EventSourcingEvent>
    ) = mutex.withLock {
        val key = SubscriptionKey(subscription)
        val list = eventLog.getOrPut(key) { mutableListOf() }

        val lastKnown = list.lastOrNull()?.eventId ?: 0L
        val firstIncoming = events.first().eventId
        require(firstIncoming == lastKnown + 1L) {
            "Event ordering violation: got $firstIncoming but expected ${lastKnown + 1L}"
        }
        list.addAll(events)
        return@withLock
    }

    override suspend fun lastEventId(subscription: TopicSubscription<*>): Long = mutex.withLock {
        eventLog[SubscriptionKey(subscription)]?.lastOrNull()?.eventId ?: 0L
    }

    override suspend fun eventsSince(
        subscription: TopicSubscription<*>,
        eventId: Long
    ): List<EventSourcingEvent> = mutex.withLock {
        val key = SubscriptionKey(subscription)
        val events = eventLog[key] ?: return emptyList()
        events.filter { it.eventId > eventId }
    }

    override suspend fun clearAll() = mutex.withLock {
        eventLog.clear()
    }

    private data class SubscriptionKey(
        val groupId: String,
        val topicValue: String
    ) {
        constructor(s: TopicSubscription<*>) : this(s.groupId, s.topic.value)
    }
}
