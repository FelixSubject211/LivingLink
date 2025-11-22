package felix.projekt.livinglink.composeApp.eventSourcing.domain

import felix.projekt.livinglink.composeApp.core.domain.NetworkError
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.EventSourcingEvent

sealed class TopicEventsState {
    data class Error(val error: NetworkError) : TopicEventsState()

    data object Loading : TopicEventsState()

    data class Data(
        val events: List<EventSourcingEvent>,
        val totalEvents: Long
    ) : TopicEventsState()
}