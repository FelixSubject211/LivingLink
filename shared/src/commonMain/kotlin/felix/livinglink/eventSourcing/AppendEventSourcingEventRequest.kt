package felix.livinglink.eventSourcing

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

@Serializable
class AppendEventSourcingEventRequest(
    val groupId: String,
    @Polymorphic val payload: EventSourcingEvent.Payload
)