package felix.projekt.livinglink.composeApp.eventSourcing.interfaces

import kotlinx.serialization.KSerializer

interface Projector<TState, TTopic : EventTopic> {
    val id: String
    val subscription: TopicSubscription<TTopic>
    val stateSerializer: KSerializer<TState>

    fun apply(event: EventSourcingEvent): ApplyResult<TState>

    sealed class ApplyResult<TState> {
        data class Add<TState>(
            val id: String,
            val state: TState
        ) : ApplyResult<TState>()

        data class Update<TState>(
            val id: String,
            val update: (TState) -> TState
        ) : ApplyResult<TState>()

        data class Delete<TState>(
            val id: String
        ) : ApplyResult<TState>()
    }
}