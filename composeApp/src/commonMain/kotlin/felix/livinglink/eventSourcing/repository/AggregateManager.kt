package felix.livinglink.eventSourcing.repository

import felix.livinglink.common.model.LivingLinkError
import felix.livinglink.common.model.RepositoryState
import felix.livinglink.common.model.UnknownError
import felix.livinglink.common.model.dataOrNull
import felix.livinglink.event.eventbus.EventBus
import felix.livinglink.eventSourcing.EventSourcingEvent
import felix.livinglink.eventSourcing.UserAnonymized
import felix.livinglink.eventSourcing.filterByPayloadType
import felix.livinglink.eventSourcing.store.AggregateStore
import felix.livinglink.eventSourcing.store.EventStore
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.reflect.KClass

class AggregateManager<
        PAYLOAD : EventSourcingEvent.Payload,
        AGGREGATE : Aggregate<AGGREGATE, PAYLOAD>
        >(
    private val groupId: String,
    private val aggregationKey: String,
    private val payloadType: KClass<PAYLOAD>,
    private val initial: AGGREGATE,
    private val eventStore: EventStore,
    private val aggregateStore: AggregateStore,
    private val eventBus: EventBus,
    private val incomingEvents: Flow<List<EventSourcingEvent<*>>?>,
    private val scope: CoroutineScope
) {
    private val rebuildDone = CompletableDeferred<Unit>()
    private val state = MutableStateFlow<RepositoryState<AGGREGATE, LivingLinkError>>(
        RepositoryState.Loading(data = null)
    )
    private val isLoading = MutableStateFlow(value = true)
    private val mutex = Mutex()

    val output = combine(state, isLoading) { state, isLoading ->
        if (isLoading) {
            RepositoryState.Loading(state.dataOrNull())
        } else {
            state
        }
    }.distinctUntilChanged()

    init {
        scope.launch {
            try {
                val snapshot = loadOrRebuild()
                state.value = if (snapshot.aggregate.isEmpty())
                    RepositoryState.Empty else RepositoryState.Data(snapshot.aggregate)

                scope.launch {
                    incomingEvents.collect { batch ->
                        applyAndPersist(batch)
                    }
                }

                rebuildDone.complete(Unit)

                eventBus.triggerUpdateNow()
            } catch (e: Exception) {
                state.value = RepositoryState.Error(UnknownError(e))
            }
        }
    }

    suspend fun clear() = mutex.withLock {
        scope.cancel()
        aggregateStore.clear(cacheKey())
    }

    private suspend fun loadOrRebuild(): AggregateSnapshot<AGGREGATE> {
        val key = cacheKey()
        val existingSnapshot = aggregateStore.get(key, initial.serializer())
        val base = existingSnapshot?.aggregate ?: initial
        val lastSeenId = existingSnapshot?.lastSeenGlobalEventId ?: -1L

        val newEvents = eventStore.getEventsSince(groupId, lastSeenId)
            .filterByPayloadType(payloadType)

        val updated = newEvents.fold(base) { acc, ev -> acc.applyEvent(ev) }
        val latestId = newEvents.lastOrNull()?.eventId ?: lastSeenId

        val snapshot = AggregateSnapshot(updated, latestId)
        aggregateStore.store(key, initial.serializer(), snapshot)
        return snapshot
    }

    private suspend fun applyAndPersist(events: List<EventSourcingEvent<*>>?) = mutex.withLock {
        rebuildDone.await()

        if (events == null) {
            isLoading.value = false
            return@withLock
        }

        val relevant = events.filterByPayloadType(payloadType)
        if (relevant.isEmpty()) return

        val current = when (val s = state.value) {
            is RepositoryState.Data -> s.data
            is RepositoryState.Loading -> s.data ?: initial
            else -> initial
        }

        val updated = relevant.fold(current) { acc, ev -> acc.applyEvent(ev) }
        val latestId = events.maxOf { it.eventId }

        val snapshot = AggregateSnapshot(updated, latestId)
        aggregateStore.store(cacheKey(), initial.serializer(), snapshot)
        state.value = RepositoryState.Data(updated)
        isLoading.value = false

        events.filterByPayloadType(UserAnonymized::class).forEach { event ->
            val data = state.value.dataOrNull()!!
            val new = data.anonymizeUser(event.payload.originalUserId)
            aggregateStore.store(cacheKey(), initial.serializer(), new)
            state.value = RepositoryState.Data(new)
        }
    }

    private fun cacheKey() = CacheKey(
        groupId = groupId,
        aggregationKey = aggregationKey,
        qualifiedTypeName = payloadType.qualifiedName!!
    )
}