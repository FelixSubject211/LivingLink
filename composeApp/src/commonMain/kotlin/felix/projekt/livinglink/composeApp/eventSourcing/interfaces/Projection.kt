package felix.projekt.livinglink.composeApp.eventSourcing.interfaces

import felix.projekt.livinglink.composeApp.core.domain.PagingModel
import kotlinx.coroutines.flow.Flow

interface Projection<TState> {
    fun status(): Flow<State<Unit>>
    fun item(id: String): Flow<State<TState?>>
    fun page(): PagingModel<TState>

    sealed class State<out STATE> {
        data class Loading(val progress: Float) : State<Nothing>()
        data class Data<STATE>(
            val state: STATE,
            val lastEventId: Long
        ) : State<STATE>()
    }

    data class Page<TState>(
        val items: Map<String, TState>,
        val offset: Int,
        val limit: Int,
        val totalCount: Int
    )
}
