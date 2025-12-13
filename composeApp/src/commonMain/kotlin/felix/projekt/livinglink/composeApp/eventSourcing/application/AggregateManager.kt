package felix.projekt.livinglink.composeApp.eventSourcing.application

import felix.projekt.livinglink.composeApp.eventSourcing.domain.EventBatch
import felix.projekt.livinglink.composeApp.eventSourcing.domain.EventStore
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.Aggregator
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.EventAggregateState
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.EventSourcingEvent
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.EventTopic
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

    val state: StateFlow<EventAggregateState<TState>> = flow {
        var currentState = aggregator.initialState
        var lastAppliedEventId = 0L
        var appliedEventCount = 0

        suspend fun emitData() {
            emit(
                EventAggregateState.Data(
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

        val initialMissing = eventStore.eventsSince(
            subscription = aggregator.subscription,
            eventId = lastAppliedEventId
        )

        if (initialMissing.isNotEmpty()) {
            applyEvents(initialMissing)
            emitData()
        }

        updates.collect { update ->
            when (update) {
                EventBatch.NoChange -> {
                    emitData()
                }

                is EventBatch.Local -> {
                    if (update.newEvent.eventId != lastAppliedEventId + 1L) {
                        val missing = eventStore.eventsSince(
                            subscription = aggregator.subscription,
                            eventId = lastAppliedEventId
                        )
                        applyEvents(missing)
                    }

                    applyEvents(listOf(update.newEvent))
                    emitData()
                }

                is EventBatch.Remote -> {
                    val firstIncomingId = update.newEvents.first().eventId

                    if (firstIncomingId != lastAppliedEventId + 1L) {
                        val missing = eventStore.eventsSince(
                            subscription = aggregator.subscription,
                            eventId = lastAppliedEventId
                        )
                        applyEvents(missing)
                    }

                    applyEvents(update.newEvents)

                    if (update.totalEvents > appliedEventCount) {
                        val ratio = appliedEventCount.toFloat() / update.totalEvents.toFloat()
                        emit(
                            EventAggregateState.Loading(progress = ratio.coerceIn(0f, 1f))
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
        initialValue = EventAggregateState.Loading(progress = 0f)
    )
}