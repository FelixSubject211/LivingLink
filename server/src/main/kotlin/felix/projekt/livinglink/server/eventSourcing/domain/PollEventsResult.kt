package felix.projekt.livinglink.server.eventSourcing.domain

sealed class PollEventsResult {
    data class Success(
        val events: List<EventSourcingEvent>,
        val totalEvents: Long
    ) : PollEventsResult()

    object NotModified : PollEventsResult()

    object NotAuthorized : PollEventsResult()
}