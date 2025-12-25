package felix.projekt.livinglink.composeApp.eventSourcing.application

import felix.projekt.livinglink.composeApp.core.domain.PagingModel
import felix.projekt.livinglink.composeApp.eventSourcing.domain.ProjectionStore
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.Projection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProjectionPagingModel<T>(
    private val store: ProjectionStore<T>,
    private val runner: Flow<*>,
    private val scope: CoroutineScope,
    private val pageSize: Int = 100
) : PagingModel<T> {

    private val internalState = MutableStateFlow<PagingModel.State<T>>(
        PagingModel.State.Loading(progress = 0.0f)
    )

    override val state: StateFlow<PagingModel.State<T>> = runner
        .onStart {
            loadNextItems()
        }
        .combine(internalState) { _, pagingState ->
            pagingState
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = PagingModel.State.Loading(0.0f)
        )

    private var restoredIndex: Int? = null
    private var offset: Int = 0
    private var pageJob: Job? = null

    override fun loadNextItems() {
        pageJob?.cancel()

        pageJob = scope.launch {
            store.page(
                offset = 0,
                limit = pageSize + offset
            ).collect { projectionState ->
                when (projectionState) {
                    is Projection.State.Loading -> {
                        internalState.value = PagingModel.State.Loading(
                            progress = projectionState.progress
                        )
                    }

                    is Projection.State.Data -> {
                        val page = projectionState.state
                        val newItems = page.items.values.toList()

                        offset += newItems.size

                        internalState.value = PagingModel.State.Data(
                            items = newItems
                        )
                    }
                }
            }
        }
    }

    override fun restoreFromIndex(firstIndex: Int) {
        if (restoredIndex != null) {
            return
        }

        restoredIndex = firstIndex
        offset = firstIndex
        loadNextItems()
    }
}
