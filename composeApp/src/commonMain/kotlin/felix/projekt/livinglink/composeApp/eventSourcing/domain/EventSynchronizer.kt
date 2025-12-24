package felix.projekt.livinglink.composeApp.eventSourcing.domain

import felix.projekt.livinglink.composeApp.core.domain.NetworkError
import felix.projekt.livinglink.composeApp.core.domain.Result
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.TopicSubscription
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.serialization.json.JsonElement

interface EventSynchronizer {
    fun subscribe(subscription: TopicSubscription<*>): SharedFlow<EventBatch>

    suspend fun appendEvent(
        subscription: TopicSubscription<*>,
        payload: JsonElement,
        expectedLastEventId: Long
    ): Result<AppendEventResponse, NetworkError>

    suspend fun clear()
}