package felix.projekt.livinglink.shared.eventSourcing.requestModel

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

sealed interface EventSourcingRequest {

    @Serializable
    data class Append(
        val groupId: String,
        val topic: String,
        val payload: JsonElement,
        val expectedLastEventId: Long
    )

    @Serializable
    data class Poll(
        val groupId: String,
        val topic: String,
        val lastKnownEventId: Long
    )
}