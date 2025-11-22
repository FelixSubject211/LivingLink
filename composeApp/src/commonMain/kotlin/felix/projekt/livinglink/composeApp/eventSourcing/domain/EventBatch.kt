package felix.projekt.livinglink.composeApp.eventSourcing.domain

import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.EventSourcingEvent

sealed class EventBatch {
    data object NoChange : EventBatch()
    data class Local(val newEvent: EventSourcingEvent) : EventBatch()
    data class Remote(
        val newEvents: List<EventSourcingEvent>,
        val totalEvents: Long
    ) : EventBatch()
}