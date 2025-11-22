package felix.projekt.livinglink.composeApp.eventSourcing.domain

import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.EventSourcingEvent
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.TopicSubscription

interface EventStore {
    suspend fun append(subscription: TopicSubscription<*>, events: List<EventSourcingEvent>)
    suspend fun lastEventId(subscription: TopicSubscription<*>): Long
    suspend fun eventsSince(subscription: TopicSubscription<*>, eventId: Long): List<EventSourcingEvent>
    suspend fun clearAll()
}