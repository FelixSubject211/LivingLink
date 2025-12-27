package felix.projekt.livinglink.server.eventSourcing.application

import felix.projekt.livinglink.server.eventSourcing.domain.EventSourcingRepository
import felix.projekt.livinglink.server.eventSourcing.interfaces.AnonymizeUserEventsService

class AnonymizeUserEventsDefaultService(
    private val eventSourcingRepository: EventSourcingRepository
) : AnonymizeUserEventsService {
    override suspend fun invoke(groupId: String, userId: String) {
        eventSourcingRepository.anonymizeUserEvents(
            groupId = groupId,
            userId = userId,
            anonymizedUserId = "Anonymized"
        )
    }
}