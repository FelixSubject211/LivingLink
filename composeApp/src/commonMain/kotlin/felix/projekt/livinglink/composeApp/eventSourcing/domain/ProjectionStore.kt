package felix.projekt.livinglink.composeApp.eventSourcing.domain

import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.Projection
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.Projector
import kotlinx.coroutines.flow.Flow

interface ProjectionStore<TState> {
    suspend fun lastEventId(): Long
    suspend fun appliedEventCount(): Long
    fun status(): Flow<Projection.State<Unit>>
    fun item(id: String): Flow<Projection.State<TState?>>
    fun page(offset: Int, limit: Int): Flow<Projection.State<Projection.Page<TState>>>
    fun apply(
        results: List<Projector.ApplyResult<TState>>,
        lastEventId: Long,
        loadingProgress: Float?
    )

    suspend fun clearAll()
}
