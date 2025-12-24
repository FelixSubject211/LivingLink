package felix.projekt.livinglink.composeApp.eventSourcing.application

import felix.projekt.livinglink.composeApp.AppConfig
import felix.projekt.livinglink.composeApp.core.domain.NetworkError
import felix.projekt.livinglink.composeApp.core.domain.Result
import felix.projekt.livinglink.composeApp.core.domain.withLockNonSuspend
import felix.projekt.livinglink.composeApp.eventSourcing.domain.AppendEventResponse
import felix.projekt.livinglink.composeApp.eventSourcing.domain.EventBatch
import felix.projekt.livinglink.composeApp.eventSourcing.domain.EventSourcingNetworkDataSource
import felix.projekt.livinglink.composeApp.eventSourcing.domain.EventStore
import felix.projekt.livinglink.composeApp.eventSourcing.domain.EventSynchronizer
import felix.projekt.livinglink.composeApp.eventSourcing.domain.PollEventsResponse
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.EventSourcingEvent
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.TopicSubscription
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonElement

class OnlineEventSynchronizer(
    private val eventStore: EventStore,
    private val eventSourcingNetworkDataSource: EventSourcingNetworkDataSource,
    private val scope: CoroutineScope
) : EventSynchronizer {
    private val mutex = Mutex()
    private val sharedFlows = HashMap<TopicSubscription<*>, SharedFlow<EventBatch>>()
    private val manualAppendChannels = HashMap<TopicSubscription<*>, Channel<EventSourcingEvent?>>()

    override fun subscribe(subscription: TopicSubscription<*>): SharedFlow<EventBatch> = mutex.withLockNonSuspend {
        sharedFlows.getOrPut(subscription) {
            pollEvents(subscription)
                .shareIn(
                    scope = scope,
                    started = SharingStarted.WhileSubscribed(
                        stopTimeoutMillis = 5_000
                    )
                )
        }
    }

    override suspend fun appendEvent(
        subscription: TopicSubscription<*>,
        payload: JsonElement,
        expectedLastEventId: Long
    ): Result<AppendEventResponse, NetworkError> = mutex.withLock {
        val result = eventSourcingNetworkDataSource.appendEvent(
            groupId = subscription.groupId,
            topic = subscription.topic.value,
            payload = payload,
            expectedLastEventId = expectedLastEventId
        )

        val channel = getOrCreateManualChannel(subscription)
        channel.send(
            (result as? Result.Success)
                ?.data
                ?.let { it as? AppendEventResponse.Success }
                ?.event
        )
        result
    }

    override suspend fun clear() {
        mutex.withLock {
            eventStore.clearAll()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun pollEvents(subscription: TopicSubscription<*>): Flow<EventBatch> = flow {
        val manualChannel = getOrCreateManualChannel(subscription)
        var nextDelay = 0L

        while (scope.isActive) {
            var skipPolling = false

            select {
                onTimeout(nextDelay) {}

                manualChannel.onReceive { event ->
                    val batch = handleManualAppend(subscription, event)
                    if (batch != null) {
                        emit(batch)
                        skipPolling = true
                    }
                }
            }

            if (skipPolling) continue

            val result = pollFromNetwork(subscription)
            val (batch, delay) = handlePollResult(subscription, result)

            nextDelay = delay
            if (batch != null) {
                emit(batch)
            }
        }
    }

    private fun getOrCreateManualChannel(
        subscription: TopicSubscription<*>
    ): Channel<EventSourcingEvent?> {
        return manualAppendChannels.getOrPut(subscription) {
            Channel(Channel.UNLIMITED)
        }
    }


    private suspend fun handleManualAppend(
        subscription: TopicSubscription<*>,
        event: EventSourcingEvent?
    ): EventBatch? = mutex.withLock {
        val lastEventId = eventStore.lastEventId(subscription)

        if (event != null && lastEventId + 1L == event.eventId) {
            eventStore.append(subscription, listOf(event))
            EventBatch.Local(event)
        } else {
            null
        }
    }

    private suspend fun pollFromNetwork(
        subscription: TopicSubscription<*>
    ): Result<PollEventsResponse, NetworkError> {
        return eventSourcingNetworkDataSource.pollEvents(
            groupId = subscription.groupId,
            topic = subscription.topic.value,
            lastKnownEventId = eventStore.lastEventId(subscription)
        )
    }


    private suspend fun handlePollResult(
        subscription: TopicSubscription<*>,
        result: Result<PollEventsResponse, NetworkError>
    ): Pair<EventBatch?, Long> = mutex.withLock {
        when (result) {
            is Result.Success -> when (val data = result.data) {
                is PollEventsResponse.Success -> {
                    val lastEventId = eventStore.lastEventId(subscription)
                    val firstIncoming = data.events.firstOrNull()?.eventId

                    val batch = if (firstIncoming != null && lastEventId + 1L == firstIncoming) {
                        eventStore.append(subscription, data.events)
                        EventBatch.Remote(
                            newEvents = data.events,
                            totalEvents = data.totalEvents
                        )
                    } else {
                        null
                    }
                    batch to data.nextPollAfterMillis
                }

                is PollEventsResponse.NotModified -> {
                    EventBatch.NoChange to data.nextPollAfterMillis
                }

                else -> {
                    null to AppConfig.eventSourcingPollFallbackMills
                }
            }

            is Result.Error -> {
                null to AppConfig.eventSourcingPollFallbackMills
            }
        }
    }
}