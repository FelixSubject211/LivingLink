package felix.projekt.livinglink.shared.eventSourcing.requestModel

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

sealed interface EventSourcingResponse {

    @Serializable
    sealed class AppendEvent {
        @Serializable
        data class Success(
            val event: Event
        ) : AppendEvent()

        @Serializable
        data object VersionMismatch : AppendEvent()

        @Serializable
        data object NotAuthorized : AppendEvent()
    }

    @Serializable
    sealed class PollEvents {
        @Serializable
        data class Success(
            val events: List<Event>,
            val totalEvents: Long,
            val nextPollAfterMillis: Long
        ) : PollEvents()

        @Serializable
        data class NotModified(
            val nextPollAfterMillis: Long
        ) : PollEvents()

        @Serializable
        data object NotAuthorized : PollEvents()
    }

    @Serializable
    data class Event(
        val eventId: Long,
        val groupId: String,
        val topic: String,
        val createdBy: String,
        val createdAtEpochMillis: Long,
        val payload: JsonElement
    )
}