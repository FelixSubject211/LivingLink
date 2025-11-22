package felix.projekt.livinglink.composeApp.eventSourcing.infrastructure

import felix.projekt.livinglink.composeApp.core.domain.NetworkError
import felix.projekt.livinglink.composeApp.core.domain.Result
import felix.projekt.livinglink.composeApp.core.domain.map
import felix.projekt.livinglink.composeApp.core.infrastructure.post
import felix.projekt.livinglink.composeApp.eventSourcing.domain.AppendEventResponse
import felix.projekt.livinglink.composeApp.eventSourcing.domain.EventSourcingNetworkDataSource
import felix.projekt.livinglink.composeApp.eventSourcing.domain.PollEventsResponse
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.EventSourcingEvent
import felix.projekt.livinglink.shared.eventSourcing.requestModel.EventSourcingRequest
import felix.projekt.livinglink.shared.eventSourcing.requestModel.EventSourcingResponse
import io.ktor.client.HttpClient
import kotlinx.serialization.json.JsonElement

class EventSourcingNetworkDefaultDataSource(
    private val httpClient: HttpClient
) : EventSourcingNetworkDataSource {
    override suspend fun appendEvent(
        groupId: String,
        topic: String,
        payload: JsonElement,
        expectedLastEventId: Long
    ): Result<AppendEventResponse, NetworkError> {
        return httpClient.post<EventSourcingRequest.Append, EventSourcingResponse.AppendEvent>(
            urlString = "event-sourcing/append",
            request = EventSourcingRequest.Append(
                groupId = groupId,
                topic = topic,
                payload = payload,
                expectedLastEventId = expectedLastEventId
            )
        ).map { response ->
            when (response) {
                is EventSourcingResponse.AppendEvent.Success -> {
                    AppendEventResponse.Success(event = response.event.toDomain())
                }

                is EventSourcingResponse.AppendEvent.VersionMismatch -> {
                    AppendEventResponse.VersionMismatch
                }

                is EventSourcingResponse.AppendEvent.NotAuthorized -> {
                    AppendEventResponse.NotAuthorized
                }
            }
        }
    }

    override suspend fun pollEvents(
        groupId: String,
        topic: String,
        lastKnownEventId: Long
    ): Result<PollEventsResponse, NetworkError> {
        return httpClient.post<EventSourcingRequest.Poll, EventSourcingResponse.PollEvents>(
            urlString = "event-sourcing/poll",
            request = EventSourcingRequest.Poll(
                groupId = groupId,
                topic = topic,
                lastKnownEventId = lastKnownEventId
            )
        ).map { response ->
            when (response) {
                is EventSourcingResponse.PollEvents.Success -> {
                    PollEventsResponse.Success(
                        events = response.events.map { it.toDomain() },
                        totalEvents = response.totalEvents,
                        nextPollAfterMillis = response.nextPollAfterMillis
                    )
                }

                is EventSourcingResponse.PollEvents.NotModified -> {
                    PollEventsResponse.NotModified(
                        nextPollAfterMillis = response.nextPollAfterMillis
                    )
                }

                is EventSourcingResponse.PollEvents.NotAuthorized -> {
                    PollEventsResponse.NotAuthorized
                }
            }
        }
    }

    private fun EventSourcingResponse.Event.toDomain() = EventSourcingEvent(
        eventId = eventId,
        groupId = groupId,
        topic = topic,
        createdBy = createdBy,
        createdAtEpochMillis = createdAtEpochMillis,
        payload = payload
    )
}
