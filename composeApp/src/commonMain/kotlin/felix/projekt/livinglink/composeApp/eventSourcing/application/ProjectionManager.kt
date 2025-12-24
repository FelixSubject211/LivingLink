package felix.projekt.livinglink.composeApp.eventSourcing.application

import felix.projekt.livinglink.composeApp.AppConfig
import felix.projekt.livinglink.composeApp.eventSourcing.domain.EventBatch
import felix.projekt.livinglink.composeApp.eventSourcing.domain.EventStore
import felix.projekt.livinglink.composeApp.eventSourcing.domain.ProjectionStore
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.EventSourcingEvent
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.EventTopic
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.Projection
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.Projector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.job

class ProjectionManager<TState, TTopic : EventTopic>(
    private val projector: Projector<TState, TTopic>,
    private val projectionStore: ProjectionStore<TState>,
    private val updates: Flow<EventBatch>,
    private val eventStore: EventStore,
    private val parentScope: CoroutineScope
) : Projection<TState> {
    private val job = SupervisorJob(parentScope.coroutineContext.job)
    private val scope = CoroutineScope(parentScope.coroutineContext + job)
    private val isReplaying = MutableStateFlow(false)

    suspend fun stop() {
        job.cancel()
        eventStore.clearAll()
        projectionStore.clearAll()
    }

    private val runner: SharedFlow<EventBatch> = updates
        .onStart {
            lastAppliedEventId = projectionStore.lastEventId()
            appliedEventCount = projectionStore.appliedEventCount()

            replayMissingEvents()

            handleBatch(EventBatch.NoChange)
        }
        .onEach { batch ->
            handleBatch(batch)
        }
        .shareIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(),
            replay = 1
        )

    private var lastAppliedEventId = 0L
    private var appliedEventCount = 0L

    private suspend fun replayMissingEvents() {
        isReplaying.value = true

        while (true) {
            val missing = eventStore.eventsSince(
                subscription = projector.subscription,
                eventId = lastAppliedEventId,
                limit = AppConfig.eventSourcingEventBatchSize
            )
            if (missing.isEmpty()) {
                break
            }

            applyEvents(missing, 0.0F)
        }

        isReplaying.value = false
    }

    private fun applyEvents(events: List<EventSourcingEvent>, loadingProgress: Float?) {
        val newLastEventId = if (events.isNotEmpty()) {
            events.last().eventId
        } else {
            lastAppliedEventId
        }

        projectionStore.apply(
            results = events.map { projector.apply(it) },
            lastEventId = newLastEventId,
            loadingProgress = loadingProgress
        )

        lastAppliedEventId = newLastEventId
        appliedEventCount += events.size
    }


    private suspend fun handleBatch(batch: EventBatch) {
        when (batch) {
            EventBatch.NoChange -> {
                applyEvents(emptyList(), null)
            }

            is EventBatch.Local -> {
                val event = batch.newEvent

                if (event.eventId != lastAppliedEventId + 1L) {
                    replayMissingEvents()
                }

                applyEvents(listOf(event), null)
            }

            is EventBatch.Remote -> {
                val firstIncomingId = batch.newEvents.first().eventId
                if (firstIncomingId != lastAppliedEventId + 1L) {
                    replayMissingEvents()
                }

                val projectedTotal = appliedEventCount + batch.newEvents.size
                val progress = if (projectedTotal < batch.totalEvents) {
                    appliedEventCount.toFloat() / batch.totalEvents.toFloat()
                } else {
                    null
                }

                applyEvents(batch.newEvents, progress)
            }
        }
    }

    override fun status(): Flow<Projection.State<Unit>> {
        return replayAwareState(projectionStore.status())
    }

    override fun item(id: String): Flow<Projection.State<TState?>> {
        return replayAwareState(projectionStore.item(id))
    }


    override fun page(offset: Int, limit: Int): Flow<Projection.State<Projection.Page<TState>>> {
        return replayAwareState(projectionStore.page(offset = offset, limit = limit))
    }

    private fun <T> replayAwareState(
        source: Flow<Projection.State<T>>
    ): StateFlow<Projection.State<T>> {
        return combine(
            runner,
            source,
            isReplaying
        ) { _, state, replaying ->
            if (replaying) {
                Projection.State.Loading(
                    progress = (state as? Projection.State.Loading)?.progress ?: 0f
                )
            } else {
                state
            }
        }.stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = Projection.State.Loading(0f)
        )
    }
}
