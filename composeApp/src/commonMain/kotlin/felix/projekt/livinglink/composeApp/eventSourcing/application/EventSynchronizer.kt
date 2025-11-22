package felix.projekt.livinglink.composeApp.eventSourcing.application

import felix.projekt.livinglink.composeApp.AppConfig
import felix.projekt.livinglink.composeApp.core.domain.NetworkError
import felix.projekt.livinglink.composeApp.core.domain.Result
import felix.projekt.livinglink.composeApp.eventSourcing.domain.AppendEventResponse
import felix.projekt.livinglink.composeApp.eventSourcing.domain.EventBatch
import felix.projekt.livinglink.composeApp.eventSourcing.domain.EventSourcingNetworkDataSource
import felix.projekt.livinglink.composeApp.eventSourcing.domain.EventStore
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

class EventSynchronizer(
    private val eventStore: EventStore,
    private val eventSourcingNetworkDataSource: EventSourcingNetworkDataSource,
    private val scope: CoroutineScope
) {
    private val sharedFlows = HashMap<TopicSubscription<*>, SharedFlow<EventBatch>>()
    private val manualAppendChannels = HashMap<TopicSubscription<*>, Channel<EventSourcingEvent?>>()
    private val pollMutex = Mutex()

    fun subscribe(subscription: TopicSubscription<*>): SharedFlow<EventBatch> {
        return sharedFlows.getOrPut(subscription) {
            pollEvents(subscription)
                .shareIn(
                    scope = scope,
                    started = SharingStarted.WhileSubscribed(
                        stopTimeoutMillis = 5_000
                    )
                )
        }
    }

    suspend fun appendEvent(
        subscription: TopicSubscription<*>,
        payload: JsonElement,
        expectedLastEventId: Long
    ): Result<AppendEventResponse, NetworkError> {
        pollMutex.withLock {
            val result = eventSourcingNetworkDataSource.appendEvent(
                groupId = subscription.groupId,
                topic = subscription.topic.value,
                payload = payload,
                expectedLastEventId = expectedLastEventId
            )

            val channel = manualAppendChannels.getOrPut(subscription) {
                Channel(Channel.UNLIMITED)
            }
            if (result is Result.Success && result.data is AppendEventResponse.Success) {
                channel.send(result.data.event)
            } else {
                channel.send(null)
            }

            return result
        }
    }

    suspend fun clear() {
        eventStore.clearAll()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun pollEvents(subscription: TopicSubscription<*>): Flow<EventBatch> = flow {
        val manualChannel = manualAppendChannels.getOrPut(subscription) {
            Channel(Channel.UNLIMITED)
        }

        var nextDelay = 0L

        while (scope.isActive) {
            var skipPolling = false

            select {
                onTimeout(nextDelay) {}

                manualChannel.onReceive { event ->
                    val shouldSkip = pollMutex.withLock {
                        val lastEventId = eventStore.lastEventId(subscription)
                        if (event != null && lastEventId + 1 == event.eventId) {
                            eventStore.append(subscription, listOf(event))
                            emit(EventBatch.Local(event))
                            true
                        } else {
                            false
                        }
                    }

                    if (shouldSkip) {
                        skipPolling = true
                    }
                }
            }

            if (skipPolling) {
                continue
            }

            val result = eventSourcingNetworkDataSource.pollEvents(
                groupId = subscription.groupId,
                topic = subscription.topic.value,
                lastKnownEventId = eventStore.lastEventId(subscription)
            )

            nextDelay = pollMutex.withLock {
                when (result) {
                    is Result.Success<*> -> {
                        when (val data = result.data) {
                            is PollEventsResponse.Success -> {
                                val lastEventId = eventStore.lastEventId(subscription)

                                if (lastEventId + 1 == data.events.firstOrNull()?.eventId) {
                                    eventStore.append(subscription, data.events)
                                    emit(
                                        EventBatch.Remote(
                                            newEvents = data.events,
                                            totalEvents = data.totalEvents
                                        )
                                    )
                                }

                                data.nextPollAfterMillis
                            }

                            is PollEventsResponse.NotModified -> {
                                emit(EventBatch.NoChange)
                                data.nextPollAfterMillis
                            }

                            else -> {
                                AppConfig.eventSourcingPollFallbackMills
                            }
                        }
                    }

                    is Result.Error -> {
                        AppConfig.eventSourcingPollFallbackMills
                    }
                }
            }
        }
    }
}