package felix.projekt.livinglink.server.eventSourcing.domain

sealed class AppendEventResult {
    data class Success(val event: EventSourcingEvent) : AppendEventResult()
    data object VersionMismatch : AppendEventResult()
    data object NotAuthorized : AppendEventResult()
}
