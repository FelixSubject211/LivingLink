package felix.livinglink.eventSourcing.repository

import felix.livinglink.eventSourcing.EventSourcingEvent

interface Aggregate<A> {
    fun applyEvent(event: EventSourcingEvent): A
    fun getLastEventId(): Long?
    fun isEmpty(): Boolean
}