package felix.livinglink.eventSourcing

import kotlinx.serialization.Serializable

@Serializable
class GetEventSourcingEventsResponse(
    val events: List<EventSourcingEvent<*>>
)