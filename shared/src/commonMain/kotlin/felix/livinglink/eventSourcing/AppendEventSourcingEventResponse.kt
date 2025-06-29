package felix.livinglink.eventSourcing

import kotlinx.serialization.Serializable

@Serializable
data class AppendEventSourcingEventResponse(
    val event: EventSourcingEvent<*>
)