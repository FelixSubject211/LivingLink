package felix.projekt.livinglink.composeApp.eventSourcing.domain

import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.EventSourcingEvent

sealed class PollEventsResponse {
    data class Success(
        val events: List<EventSourcingEvent>,
        val totalEvents: Long,
        val nextPollAfterMillis: Long
    ) : PollEventsResponse()

    data class NotModified(
        val nextPollAfterMillis: Long
    ) : PollEventsResponse()

    data object NotAuthorized : PollEventsResponse()
}