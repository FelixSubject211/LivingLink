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
    private val parentScope: CoroutineScope
) {
    private val job = SupervisorJob(parentScope.coroutineContext.job)
    private val scope = CoroutineScope(parentScope.coroutineContext + job)
    private var currentState = aggregator.initialState
    private var lastAppliedEventId: Long = 0
    private var appliedEventCount = 0

    fun stop() {
        job.cancel()
    }

    val state: StateFlow<EventAggregateState<TState>> = flow {
        val initialMissing = eventStore.eventsSince(aggregator.subscription, lastAppliedEventId)
        if (initialMissing.isNotEmpty()) {
            applyEvents(initialMissing)

            emit(
                EventAggregateState.Data(
                    state = currentState,
                    lastEventId = lastAppliedEventId
                )
            )
        }

        updates.collect { update ->
            when (update) {
                EventBatch.NoChange -> {
                    emit(
                        EventAggregateState.Data(
                            state = currentState,
                            lastEventId = lastAppliedEventId
                        )
                    )
                }

                is EventBatch.Local -> {
                    val e = update.newEvent

                    if (e.eventId != lastAppliedEventId + 1) {
                        val missing = eventStore.eventsSince(aggregator.subscription, lastAppliedEventId)
                        applyEvents(missing)
                    }

                    applyEvents(listOf(update.newEvent))

                    emit(
                        EventAggregateState.Data(
                            state = currentState,
                            lastEventId = lastAppliedEventId
                        )
                    )
                }

                is EventBatch.Remote -> {
                    val first = update.newEvents.first().eventId

                    if (first != lastAppliedEventId + 1) {
                        val missing = eventStore.eventsSince(aggregator.subscription, lastAppliedEventId)
                        applyEvents(missing)
                    }
                    applyEvents(update.newEvents)
                    if (update.totalEvents > appliedEventCount) {
                        val ratio = appliedEventCount.toFloat() / update.totalEvents.toFloat()
                        emit(EventAggregateState.Loading(progress = ratio.coerceIn(0f, 1f)))
                    } else {
                        emit(
                            EventAggregateState.Data(
                                state = currentState,
                                lastEventId = lastAppliedEventId
                            )
                        )
                    }
                }
            }
        }
    }.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = EventAggregateState.Loading(progress = 0f)
    )

    private fun applyEvents(events: List<EventSourcingEvent>): TState {
        if (events.isEmpty()) {
            return currentState
        }

        currentState = aggregator.apply(currentState, events)
        lastAppliedEventId = events.last().eventId
        appliedEventCount += events.size
        return currentState
    }
}