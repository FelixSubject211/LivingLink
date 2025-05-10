package felix.livinglink.eventSourcing

import kotlinx.serialization.Serializable

@Serializable
class GetEventSourcingEventsRequest(
    val groupId: String,
    val sinceEventIdExclusive: Long?
)