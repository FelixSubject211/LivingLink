package felix.projekt.livinglink.composeApp.eventSourcing.interfaces

interface GetProjectionService {
    operator fun <TState, TTopic : EventTopic> invoke(
        projector: Projector<TState, TTopic>
    ): Projection<TState>
}