package felix.projekt.livinglink.server.eventSourcing.application

import felix.projekt.livinglink.server.eventSourcing.domain.AppendEventResult
import felix.projekt.livinglink.server.eventSourcing.domain.EventSourcingRepository
import felix.projekt.livinglink.server.eventSourcing.interfaces.AppendEventUseCase
import felix.projekt.livinglink.server.groups.interfaces.CheckGroupMembershipService
import kotlinx.serialization.json.JsonElement

class AppendEventDefaultUseCase(
    private val repository: EventSourcingRepository,
    private val checkGroupMembershipService: CheckGroupMembershipService
) : AppendEventUseCase {
    override suspend fun invoke(
        userId: String,
        groupId: String,
        topic: String,
        payload: JsonElement,
        expectedLastEventId: Long
    ): AppendEventResult {
        if (!checkGroupMembershipService(userId = userId, groupId = groupId)) {
            return AppendEventResult.NotAuthorized
        }

        val event = repository.appendEvent(
            groupId = groupId,
            topic = topic,
            createdBy = userId,
            payload = payload,
            expectedLastEventId = expectedLastEventId
        ) ?: return AppendEventResult.VersionMismatch

        return AppendEventResult.Success(event = event)
    }
}
