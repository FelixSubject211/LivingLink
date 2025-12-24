package felix.projekt.livinglink.composeApp.eventSourcing.application

import felix.projekt.livinglink.composeApp.AppConfig
import felix.projekt.livinglink.composeApp.eventSourcing.domain.EventBatch
import felix.projekt.livinglink.composeApp.eventSourcing.domain.EventStore
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.Aggregator
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.EventSourcingEvent
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.EventTopic
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.GetAggregateService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.job

class AggregateManager<TTopic : EventTopic, TState>(
    private val aggregator: Aggregator<TTopic, TState>,
    private val updates: Flow<EventBatch>,
    private val eventStore: EventStore,
    parentScope: CoroutineScope
) {
    private val job = SupervisorJob(parentScope.coroutineContext.job)
    private val scope = CoroutineScope(parentScope.coroutineContext + job)

    fun stop() {
        job.cancel()
    }

    val state: StateFlow<GetAggregateService.State<TState>> = flow {
        var currentState = aggregator.initialState
        var lastAppliedEventId = 0L
        var appliedEventCount = 0

        suspend fun emitData() {
            emit(
                GetAggregateService.State.Data(
                    state = currentState,
                    lastEventId = lastAppliedEventId
                )
            )
        }

        fun applyEvents(events: List<EventSourcingEvent>) {
            if (events.isEmpty()) {
                return
            }

            currentState = aggregator.apply(currentState, events)
            lastAppliedEventId = events.last().eventId
            appliedEventCount += events.size
        }

        suspend fun replayMissingEvents() {
            while (true) {
                val missing = eventStore.eventsSince(
                    subscription = aggregator.subscription,
                    eventId = lastAppliedEventId,
                    limit = AppConfig.eventSourcingEventBatchSize
                )

                if (missing.isEmpty()) {
                    break
                }
                applyEvents(missing)
            }

            emitData()
        }

        replayMissingEvents()

        updates.collect { update ->
            when (update) {
                EventBatch.NoChange -> {
                    emitData()
                }

                is EventBatch.Local -> {
                    if (update.newEvent.eventId != lastAppliedEventId + 1L) {
                        replayMissingEvents()
                    }

                    applyEvents(listOf(update.newEvent))
                    emitData()
                }

                is EventBatch.Remote -> {
                    val firstIncomingId = update.newEvents.first().eventId

                    if (firstIncomingId != lastAppliedEventId + 1L) {
                        replayMissingEvents()
                    }

                    applyEvents(update.newEvents)

                    if (update.totalEvents > appliedEventCount) {
                        val ratio = appliedEventCount.toFloat() / update.totalEvents.toFloat()
                        emit(
                            GetAggregateService.State.Loading(progress = ratio.coerceIn(0f, 1f))
                        )
                    } else {
                        emitData()
                    }
                }
            }
        }
    }.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = GetAggregateService.State.Loading(progress = 0f)
    )
}
