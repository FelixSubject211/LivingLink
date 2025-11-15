package felix.projekt.livinglink.server.eventSourcing.application

import felix.projekt.livinglink.server.eventSourcing.domain.EventSourcingRepository
import felix.projekt.livinglink.server.eventSourcing.domain.PollEventsResult
import felix.projekt.livinglink.server.eventSourcing.interfaces.PollEventsUseCase
import felix.projekt.livinglink.server.groups.interfaces.CheckGroupMembershipService

class PollEventsDefaultUseCase(
    private val repository: EventSourcingRepository,
    private val checkGroupMembershipService: CheckGroupMembershipService,
    private val pollPageSize: Int
) : PollEventsUseCase {

    override suspend fun invoke(
        userId: String,
        groupId: String,
        topic: String,
        lastKnownEventId: Long?
    ): PollEventsResult {
        if (!checkGroupMembershipService(userId = userId, groupId = groupId)) {
            return PollEventsResult.NotAuthorized
        }

        val events = repository.fetchEvents(
            groupId = groupId,
            topic = topic,
            lastKnownEventId = lastKnownEventId,
            limit = pollPageSize
        )

        if (events.isEmpty()) {
            return PollEventsResult.NotModified
        }

        val totalEvents = repository.totalEvents(
            groupId = groupId,
            topic = topic
        )

        return PollEventsResult.Success(
            events = events,
            totalEvents = totalEvents
        )
    }
}
