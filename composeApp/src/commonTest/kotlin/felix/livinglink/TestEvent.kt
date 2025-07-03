package felix.livinglink

import felix.livinglink.eventSourcing.EventSourcingEvent
import kotlinx.serialization.Serializable

@Serializable
class TestEvent(
    val id: String
) : EventSourcingEvent.Payload