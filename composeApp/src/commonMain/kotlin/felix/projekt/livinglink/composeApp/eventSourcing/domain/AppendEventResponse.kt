package felix.projekt.livinglink.composeApp.eventSourcing.domain

import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.EventSourcingEvent

sealed class AppendEventResponse {
    data class Success(val event: EventSourcingEvent) : AppendEventResponse()
    data object VersionMismatch : AppendEventResponse()
    data object NotAuthorized : AppendEventResponse()
}