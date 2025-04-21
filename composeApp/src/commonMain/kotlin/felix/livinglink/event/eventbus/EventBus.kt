package felix.livinglink.event.eventbus

import felix.livinglink.auth.network.AuthenticatedHttpClient
import felix.livinglink.event.network.ChangeNotifierClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface EventBus {
    val events: Flow<Event>

    suspend fun emit(event: Event)

    sealed class Event {
        data object ClearAll : Event()
        data object UpdateGroups : Event()
    }
}

class DefaultEventBus(
    private val changeNotifierClient: ChangeNotifierClient,
    private val authenticatedHttpClient: AuthenticatedHttpClient,
    private val scope: CoroutineScope
) : EventBus {
    private val _events = MutableSharedFlow<EventBus.Event>(extraBufferCapacity = 1)

    init {
        scope.launch {
            var lastSession: AuthenticatedHttpClient.AuthSession? = null
            authenticatedHttpClient.session.collect { currentSession ->
                when {
                    lastSession is AuthenticatedHttpClient.AuthSession.LoggedIn &&
                            currentSession is AuthenticatedHttpClient.AuthSession.LoggedOut -> {
                        emit(EventBus.Event.ClearAll)
                    }

                    lastSession is AuthenticatedHttpClient.AuthSession.LoggedOut &&
                            currentSession is AuthenticatedHttpClient.AuthSession.LoggedIn -> {
                        emit(EventBus.Event.UpdateGroups)
                    }
                }
                lastSession = currentSession
            }
        }
    }

    private val pendingSuppressions = mutableListOf<EventBus.Event>()
    private val mutex = Mutex()

    override val events: Flow<EventBus.Event> = merge(
        _events,
        changeNotifierClient.events.mapNotNull { changeEvent ->
            when (changeEvent) {
                ChangeNotifierClient.Event.GroupChanged -> EventBus.Event.UpdateGroups
            }
        }.filter { incomingEvent ->
            mutex.withLock {
                val index = pendingSuppressions.indexOfFirst { it == incomingEvent }
                return@withLock if (index >= 0) {
                    pendingSuppressions.removeAt(index)
                    false
                } else {
                    true
                }
            }
        }
    )

    override suspend fun emit(event: EventBus.Event) {
        mutex.withLock {
            pendingSuppressions.add(event)
        }
        _events.emit(event)
    }
}