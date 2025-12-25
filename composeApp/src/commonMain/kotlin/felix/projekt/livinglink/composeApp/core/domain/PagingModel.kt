package felix.projekt.livinglink.composeApp.core.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface PagingModel<T> {
    val state: Flow<State<T>>
    fun loadNextItems()
    fun restoreFromIndex(firstIndex: Int)

    sealed class State<T> {
        data class Loading<T>(
            val progress: Float
        ) : State<T>()

        data class Data<T>(
            val items: List<T>
        ) : State<T>()
    }
}

fun <A, B> PagingModel<A>.mapItems(
    mapper: (A) -> B
): PagingModel<B> {
    val source = this

    return object : PagingModel<B> {

        override val state: Flow<PagingModel.State<B>> = source.state.map { state ->
            when (state) {
                is PagingModel.State.Loading -> {
                    PagingModel.State.Loading(progress = state.progress)
                }

                is PagingModel.State.Data -> {
                    PagingModel.State.Data(items = state.items.map(mapper))
                }
            }
        }

        override fun loadNextItems() {
            source.loadNextItems()
        }

        override fun restoreFromIndex(firstIndex: Int) {
            source.restoreFromIndex(firstIndex)
        }
    }
}