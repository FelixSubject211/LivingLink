package felix.livinglink.event.network

import felix.livinglink.Config
import felix.livinglink.common.model.LivingLinkResult
import felix.livinglink.common.network.NetworkError
import felix.livinglink.common.network.get
import felix.livinglink.common.store.createStore
import felix.livinglink.event.PollingUpdateResponse
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

interface ChangeNotifierClient {
    val events: Flow<Event>

    sealed class Event {
        data object GroupChanged : Event()
    }
}

class ChangeNotifierDefaultClient(
    private val config: Config,
    private val authenticatedHttpClient: HttpClient,
    private val scope: CoroutineScope
) : ChangeNotifierClient {
    private val _events = MutableSharedFlow<ChangeNotifierClient.Event>(1)

    private val lastGroupChangeIdStore = createStore(
        path = "lastGroupChangeId3",
        defaultValue = ""
    )

    override val events = _events.asSharedFlow()

    init {
        scope.launch {
            while (true) {
                val result: LivingLinkResult<PollingUpdateResponse, NetworkError> =
                    authenticatedHttpClient.get("event/group-change")

                when (result) {
                    is LivingLinkResult.Error<*> -> {
                        delay(config.pollingRetryDelaySeconds.toLong() * 1000)
                    }

                    is LivingLinkResult.Success<PollingUpdateResponse> -> {
                        if (result.data.changeId != lastGroupChangeIdStore.get()) {
                            lastGroupChangeIdStore.set(result.data.changeId)
                            _events.emit(ChangeNotifierClient.Event.GroupChanged)
                        }
                        delay(result.data.nextPollInSeconds.toLong() * 1000)
                    }
                }
            }
        }
    }
}