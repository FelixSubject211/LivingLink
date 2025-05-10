package felix.livinglink.event.eventbus

import felix.livinglink.auth.network.AuthenticatedHttpClient
import felix.livinglink.event.network.ChangeNotifierClient
import felix.livinglink.event.network.ChangeNotifierClient.Event
import felix.livinglink.ui.common.navigation.LivingLinkScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface EventBus {
    val currentGroupIdObserver: CurrentGroupIdObserver

    val events: Flow<Event>

    suspend fun emit(event: Event)

    sealed class Event {
        data object ClearAll : Event()
        data object UpdateGroups : Event()
        data class GroupStateUpdated(
            val groupId: String,
            val latestEventId: Long
        ) : Event()
    }

    class CurrentGroupIdObserver {
        private val screenStack = mutableListOf<LivingLinkScreen>()
        private val _currentGroupIdFlow = MutableStateFlow<String?>(null)
        val currentGroupIdFlow: StateFlow<String?> = _currentGroupIdFlow

        fun push(screen: LivingLinkScreen) {
            screenStack.add(screen)
            updateGroupId()
        }

        fun pop() {
            if (screenStack.isNotEmpty()) {
                screenStack.removeLast()
                updateGroupId()
            }
        }

        fun popAll() {
            screenStack.clear()
            _currentGroupIdFlow.value = null
        }

        private fun updateGroupId() {
            val top = screenStack.lastOrNull()
            _currentGroupIdFlow.value = when (top) {
                is LivingLinkScreen.Group -> top.groupId
                else -> null
            }
        }
    }
}

class DefaultEventBus(
    private val changeNotifierClient: ChangeNotifierClient,
    private val authenticatedHttpClient: AuthenticatedHttpClient,
    private val scope: CoroutineScope
) : EventBus {
    private val _events = MutableSharedFlow<EventBus.Event>(extraBufferCapacity = 1)

    override val currentGroupIdObserver = EventBus.CurrentGroupIdObserver()

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

        scope.launch {
            changeNotifierClient.start(currentGroupIdObserver)
        }
    }

    private val pendingSuppressions = mutableSetOf<EventBus.Event>()
    private val mutex = Mutex()

    override val events: Flow<EventBus.Event> = merge(
        _events,
        changeNotifierClient.events.mapNotNull { changeEvent ->
            when (changeEvent) {
                Event.MembershipChanged -> {
                    EventBus.Event.UpdateGroups
                }

                is Event.GroupStateUpdated -> {
                    EventBus.Event.GroupStateUpdated(
                        groupId = changeEvent.groupId,
                        latestEventId = changeEvent.latestEventId
                    )
                }
            }
        }.filter { incomingEvent ->
            mutex.withLock {
                val wasSuppressed = pendingSuppressions.remove(incomingEvent)
                !wasSuppressed
            }
        }
    )

    override suspend fun emit(event: EventBus.Event) {
        mutex.withLock {
            pendingSuppressions += event
        }
        _events.emit(event)
    }
}