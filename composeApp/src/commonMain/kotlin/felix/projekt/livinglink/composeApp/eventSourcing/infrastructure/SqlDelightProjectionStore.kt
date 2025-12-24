package felix.projekt.livinglink.composeApp.eventSourcing.infrastructure

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import felix.projekt.livinglink.composeApp.core.Database
import felix.projekt.livinglink.composeApp.eventSourcing.domain.ProjectionStore
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.Projection
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.Projector
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.TopicSubscription
import felix.projekt.livinglink.shared.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer

class SqlDelightProjectionStore<TState>(
    private val database: Database,
    private val projectionId: String,
    private val subscription: TopicSubscription<*>,
    private val stateSerializer: KSerializer<TState>
) : ProjectionStore<TState> {

    private val queries = database.eventDatabaseQueries

    private val loadingState = MutableStateFlow<Projection.State<Unit>>(
        Projection.State.Loading(progress = 0f)
    )

    override suspend fun lastEventId(): Long {
        return queries
            .selectLastEventId(
                projectionId = projectionId,
                groupId = subscription.groupId,
                topic = subscription.topic.value
            )
            .executeAsOneOrNull()
            ?: 0L
    }

    override suspend fun appliedEventCount(): Long {
        return queries
            .selectAppliedEventCount(
                projectionId = projectionId,
                groupId = subscription.groupId,
                topic = subscription.topic.value
            )
            .executeAsOneOrNull()
            ?: 0L
    }

    override fun status(): Flow<Projection.State<Unit>> {
        val dbFlow = queries
            .selectAppliedEventCount(
                projectionId = projectionId,
                groupId = subscription.groupId,
                topic = subscription.topic.value
            )
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { lastEventId ->
                Projection.State.Data(
                    state = Unit,
                    lastEventId = lastEventId ?: 0L
                )
            }

        return combine(
            loadingState,
            dbFlow
        ) { loading, data ->
            when (loading) {
                is Projection.State.Loading -> {
                    loading
                }

                is Projection.State.Data -> {
                    data
                }
            }
        }
    }

    override fun item(id: String): Flow<Projection.State<TState?>> {
        val itemFlow = queries
            .selectProjectionItem(
                projectionId = projectionId,
                groupId = subscription.groupId,
                topic = subscription.topic.value,
                itemId = id
            )
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)

        return combine(
            loadingState,
            itemFlow,
            status()
        ) { loading, row, status ->
            when (loading) {
                is Projection.State.Loading -> {
                    loading
                }

                is Projection.State.Data -> {
                    if (row == null) {
                        Projection.State.Loading(0.0F)
                    } else {
                        Projection.State.Data(
                            state = json.decodeFromString(stateSerializer, row),
                            lastEventId = (status as? Projection.State.Data)?.lastEventId ?: 0
                        )
                    }
                }
            }
        }
    }

    override fun page(
        offset: Int,
        limit: Int
    ): Flow<Projection.State<Projection.Page<TState>>> {
        val pageFlow = queries
            .selectProjectionPage(
                projectionId = projectionId,
                groupId = subscription.groupId,
                topic = subscription.topic.value,
                limit = limit.toLong(),
                offset = offset.toLong()
            )
            .asFlow()
            .mapToList(Dispatchers.IO)

        return combine(
            loadingState,
            pageFlow,
            status()
        ) { loading, rows, status ->
            when (loading) {
                is Projection.State.Loading -> {
                    loading
                }

                is Projection.State.Data -> {
                    val items = rows.associate {
                        it.itemId to json.decodeFromString(stateSerializer, it.state)
                    }

                    Projection.State.Data(
                        state = Projection.Page(
                            items = items,
                            offset = offset,
                            limit = limit,
                            totalCount = items.size
                        ),
                        lastEventId = (status as? Projection.State.Data)?.lastEventId ?: 0
                    )
                }
            }
        }
    }

    override fun apply(
        results: List<Projector.ApplyResult<TState>>,
        lastEventId: Long,
        loadingProgress: Float?
    ) {
        database.transaction {
            results.forEach { result ->
                when (result) {
                    is Projector.ApplyResult.Add -> {
                        queries.insertProjectionItemIfAbsent(
                            projectionId = projectionId,
                            groupId = subscription.groupId,
                            topic = subscription.topic.value,
                            itemId = result.id,
                            state = json.encodeToString(
                                stateSerializer,
                                result.state
                            )
                        )
                    }

                    is Projector.ApplyResult.Update -> {
                        val existing = queries
                            .selectProjectionItem(
                                projectionId,
                                subscription.groupId,
                                subscription.topic.value,
                                result.id
                            )
                            .executeAsOne()

                        val current = json.decodeFromString(
                            stateSerializer,
                            existing
                        )

                        val updated = result.update(current)

                        queries.updateProjectionItemState(
                            projectionId = projectionId,
                            groupId = subscription.groupId,
                            topic = subscription.topic.value,
                            itemId = result.id,
                            state = json.encodeToString(
                                stateSerializer,
                                updated
                            )
                        )
                    }

                    is Projector.ApplyResult.Delete -> {
                        queries.deleteProjectionItem(
                            projectionId = projectionId,
                            groupId = subscription.groupId,
                            topic = subscription.topic.value,
                            itemId = result.id
                        )
                    }
                }
            }

            val appliedEventCount = queries.selectAppliedEventCount(
                projectionId = projectionId,
                groupId = subscription.groupId,
                topic = subscription.topic.value
            ).executeAsOneOrNull() ?: 0

            queries.upsertProjectionMeta(
                projectionId = projectionId,
                groupId = subscription.groupId,
                topic = subscription.topic.value,
                lastEventId = lastEventId,
                appliedEventCount = appliedEventCount + results.size
            )
        }

        if (loadingProgress != null) {
            loadingState.value = Projection.State.Loading(
                progress = loadingProgress
            )
        } else {
            loadingState.value = Projection.State.Data(
                state = Unit,
                lastEventId = lastEventId
            )
        }
    }

    override suspend fun clearAll() {
        loadingState.value = Projection.State.Loading(progress = 0f)
    }
}