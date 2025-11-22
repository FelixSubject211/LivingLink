package felix.projekt.livinglink.server.eventSourcing.interfaces

import felix.projekt.livinglink.server.eventSourcing.domain.AppendEventResult
import kotlinx.serialization.json.JsonElement

fun interface AppendEventUseCase {
    suspend operator fun invoke(
        userId: String,
        groupId: String,
        topic: String,
        payload: JsonElement,
        expectedLastEventId: Long
    ): AppendEventResult
}
