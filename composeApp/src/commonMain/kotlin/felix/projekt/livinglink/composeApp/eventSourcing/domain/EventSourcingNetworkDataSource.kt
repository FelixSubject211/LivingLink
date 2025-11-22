package felix.projekt.livinglink.composeApp.eventSourcing.domain

import felix.projekt.livinglink.composeApp.core.domain.NetworkError
import felix.projekt.livinglink.composeApp.core.domain.Result
import kotlinx.serialization.json.JsonElement

interface EventSourcingNetworkDataSource {
    suspend fun appendEvent(
        groupId: String,
        topic: String,
        payload: JsonElement,
        expectedLastEventId: Long
    ): Result<AppendEventResponse, NetworkError>

    suspend fun pollEvents(
        groupId: String,
        topic: String,
        lastKnownEventId: Long
    ): Result<PollEventsResponse, NetworkError>
}
