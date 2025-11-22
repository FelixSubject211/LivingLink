package felix.projekt.livinglink.composeApp.eventSourcing.interfaces

import kotlinx.serialization.json.JsonElement

data class EventSourcingEvent(
    val eventId: Long,
    val groupId: String,
    val topic: String,
    val createdBy: String,
    val createdAtEpochMillis: Long,
    val payload: JsonElement
)