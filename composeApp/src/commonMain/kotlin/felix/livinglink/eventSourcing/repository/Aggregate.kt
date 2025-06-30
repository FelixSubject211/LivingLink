package felix.livinglink.eventSourcing.repository

import felix.livinglink.eventSourcing.EventSourcingEvent
import kotlinx.serialization.KSerializer

interface Aggregate<AGGREGATE, PAYLOAD : EventSourcingEvent.Payload> {
    fun applyEvent(event: EventSourcingEvent<PAYLOAD>): AGGREGATE
    fun isEmpty(): Boolean
    fun anonymizeUser(originalUserId: String): AGGREGATE
    fun serializer(): KSerializer<out AGGREGATE>
}