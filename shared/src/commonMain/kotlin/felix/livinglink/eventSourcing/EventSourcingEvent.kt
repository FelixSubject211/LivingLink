package felix.livinglink.eventSourcing

import kotlinx.datetime.Instant
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

@Serializable
data class EventSourcingEvent<PAYLOAD : EventSourcingEvent.Payload>(
    val eventId: Long,
    val userId: String,
    val groupId: String,
    val createdAt: Instant,
    @Polymorphic val payload: PAYLOAD
) {
    interface Payload
}