package felix.projekt.livinglink.composeApp.eventSourcing.application

import felix.projekt.livinglink.composeApp.eventSourcing.domain.EventSourcingRepository
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.EventTopic
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.GetProjectionService
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.Projection
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.Projector

class GetProjectionDefaultService(
    private val repository: EventSourcingRepository
) : GetProjectionService {
    override fun <TState, TTopic : EventTopic> invoke(
        projector: Projector<TState, TTopic>
    ): Projection<TState> {
        return repository.getProjection(projector)
    }
}