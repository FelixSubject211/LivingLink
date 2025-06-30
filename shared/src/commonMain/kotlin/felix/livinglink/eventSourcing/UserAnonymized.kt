package felix.livinglink.eventSourcing

import kotlinx.serialization.Serializable

@Serializable
data class UserAnonymized(
    val originalUserId: String
) : EventSourcingEvent.Payload