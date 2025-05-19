package felix.livinglink.eventSourcing

import kotlinx.datetime.Instant
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

@Serializable
data class EventSourcingEvent(
    val eventId: Long,
    val userId: String,
    val groupId: String,
    val createdAt: Instant,
    @Polymorphic val payload: Payload
) {
    interface Payload
}