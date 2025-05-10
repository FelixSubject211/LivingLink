package felix.livinglink.eventSourcing.network

import felix.livinglink.common.model.LivingLinkResult
import felix.livinglink.common.network.NetworkError
import felix.livinglink.common.network.get
import felix.livinglink.common.network.post
import felix.livinglink.eventSourcing.AppendEventSourcingEventRequest
import felix.livinglink.eventSourcing.AppendEventSourcingEventResponse
import felix.livinglink.eventSourcing.GetEventSourcingEventsResponse
import io.ktor.client.HttpClient

interface EventSourcingNetworkDataSource {
    suspend fun getEvents(
        groupId: String,
        sinceEventIdExclusive: Long?
    ): LivingLinkResult<GetEventSourcingEventsResponse, NetworkError>

    suspend fun appendEvent(
        request: AppendEventSourcingEventRequest
    ): LivingLinkResult<AppendEventSourcingEventResponse, NetworkError>
}

class EventSourcingNetworkDefaultDataSource(
    private val authenticatedHttpClient: HttpClient,
) : EventSourcingNetworkDataSource {

    override suspend fun getEvents(
        groupId: String,
        sinceEventIdExclusive: Long?
    ): LivingLinkResult<GetEventSourcingEventsResponse, NetworkError> {
        val queryParams = buildString {
            append("?groupId=$groupId")
            if (sinceEventIdExclusive != null) {
                append("&sinceEventIdExclusive=$sinceEventIdExclusive")
            }
        }

        return authenticatedHttpClient.get("eventSourcing/events$queryParams")
    }

    override suspend fun appendEvent(
        request: AppendEventSourcingEventRequest
    ): LivingLinkResult<AppendEventSourcingEventResponse, NetworkError> {
        return authenticatedHttpClient.post(
            urlString = "eventSourcing/append",
            request = request
        )
    }
}
