package felix.livinglink.event.network

import felix.livinglink.Config
import felix.livinglink.common.model.LivingLinkResult
import felix.livinglink.common.network.NetworkError
import felix.livinglink.common.network.get
import felix.livinglink.common.store.createStore
import felix.livinglink.event.PollingUpdateResponse
import felix.livinglink.event.eventbus.EventBus
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.select

interface ChangeNotifierClient {
    val events: Flow<Event>

    sealed class Event {
        data object MembershipChanged : Event()

        data class GroupStateUpdated(
            val groupId: String,
            val latestEventId: Long?
        ) : Event()
    }

    fun start(currentGroupIdObserver: EventBus.CurrentGroupIdObserver)
}

class ChangeNotifierDefaultClient(
    private val config: Config,
    private val authenticatedHttpClient: HttpClient,
    private val scope: CoroutineScope
) : ChangeNotifierClient {

    private val _events = MutableSharedFlow<ChangeNotifierClient.Event>(extraBufferCapacity = 1)
    private val lastMembershipChangeStore = createStore(
        path = "lastMembershipChangeId",
        defaultValue = ""
    )

    override val events: Flow<ChangeNotifierClient.Event> = _events.asSharedFlow()

    override fun start(currentGroupIdObserver: EventBus.CurrentGroupIdObserver) {
        val groupIdUpdates = Channel<String?>(capacity = Channel.CONFLATED)

        scope.launch {
            currentGroupIdObserver.currentGroupIdFlow.collect {
                groupIdUpdates.trySend(it)
            }
        }

        scope.launch {
            while (true) {
                val currentGroupId = currentGroupIdObserver.currentGroupIdFlow.value

                val query = currentGroupId?.let { "?groupId=$it" } ?: ""

                val result: LivingLinkResult<PollingUpdateResponse, NetworkError>? = try {
                    authenticatedHttpClient.get("event/group-change$query")
                } catch (e: Exception) {
                    null
                }

                val delayMillis = when (result) {
                    is LivingLinkResult.Success -> {
                        val data = result.data

                        if (data.membershipChangeId != lastMembershipChangeStore.get()) {
                            lastMembershipChangeStore.set(data.membershipChangeId)
                            _events.emit(ChangeNotifierClient.Event.MembershipChanged)
                        }

                        if (currentGroupId != null) {
                            _events.emit(
                                ChangeNotifierClient.Event.GroupStateUpdated(
                                    groupId = currentGroupId,
                                    latestEventId = data.latestEventId
                                )
                            )
                        }

                        data.nextPollInSeconds * 1000L
                    }

                    else -> config.pollingRetryDelaySeconds * 1000L
                }

                waitForNextPollOrGroupChange(delayMillis, groupIdUpdates)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun waitForNextPollOrGroupChange(
        delayMillis: Long,
        groupIdUpdates: Channel<String?>
    ) {
        select {
            onTimeout(delayMillis) {}
            groupIdUpdates.onReceive {}
        }
    }
}