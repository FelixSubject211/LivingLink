package felix.livinglink.eventSourcing.repository

import felix.livinglink.eventSourcing.EventSourcingEvent

interface Aggregate<AGGREGATE, PAYLOAD : EventSourcingEvent.Payload> {
    fun applyEvent(event: EventSourcingEvent<PAYLOAD>): AGGREGATE
    fun getLastEventId(): Long?
    fun isEmpty(): Boolean
}