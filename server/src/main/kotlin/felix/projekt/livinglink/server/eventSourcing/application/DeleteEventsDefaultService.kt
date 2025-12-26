package felix.projekt.livinglink.server.eventSourcing.application

import felix.projekt.livinglink.server.eventSourcing.domain.EventSourcingRepository
import felix.projekt.livinglink.server.eventSourcing.interfaces.DeleteEventsService

class DeleteEventsDefaultService(
    private val eventSourcingRepository: EventSourcingRepository
) : DeleteEventsService {
    override suspend fun invoke(groupId: String) {
        eventSourcingRepository.deleteGroupEvents(groupId = groupId)
    }
}