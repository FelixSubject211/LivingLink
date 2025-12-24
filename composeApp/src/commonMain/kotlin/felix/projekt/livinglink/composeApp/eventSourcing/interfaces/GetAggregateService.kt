package felix.projekt.livinglink.composeApp.eventSourcing.interfaces

import kotlinx.coroutines.flow.StateFlow

interface GetAggregateService {
    operator fun <TTopic : EventTopic, TState> invoke(
        aggregator: Aggregator<TTopic, TState>
    ): StateFlow<State<TState>>

    sealed class State<out STATE> {
        data class Loading(val progress: Float) : State<Nothing>()
        data class Data<STATE>(
            val state: STATE,
            val lastEventId: Long
        ) : State<STATE>()
    }
}