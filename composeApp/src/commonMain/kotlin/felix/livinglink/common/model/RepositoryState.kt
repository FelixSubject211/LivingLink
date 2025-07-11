package felix.livinglink.common.model

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

sealed class RepositoryState<out DATA, out ERROR> {
    data object Empty : RepositoryState<Nothing, Nothing>()
    data class Loading<out DATA>(val data: DATA?) : RepositoryState<DATA, Nothing>()
    data class Error<out ERROR>(val error: ERROR) : RepositoryState<Nothing, ERROR>()
    data class Data<out DATA, out ERROR>(val data: DATA) : RepositoryState<DATA, ERROR>()
}

fun <DATA, ERROR> RepositoryState<DATA, ERROR>.dataOrNull(): DATA? {
    return when (this) {
        RepositoryState.Empty -> null
        is RepositoryState.Error -> null
        is RepositoryState.Loading -> this.data
        is RepositoryState.Data -> this.data
    }
}

fun <DATA1, DATA2, ERROR> Flow<RepositoryState<DATA1, ERROR>>.mapState(
    transform: (DATA1) -> DATA2?
): Flow<RepositoryState<DATA2, ERROR>> {
    return this.map { it.mapState(transform) }
}

fun <DATA1, DATA2, ERROR> RepositoryState<DATA1, ERROR>.mapState(
    transform: (DATA1) -> DATA2?
): RepositoryState<DATA2, ERROR> {
    return when (this) {
        RepositoryState.Empty -> {
            RepositoryState.Empty
        }

        is RepositoryState.Loading -> {
            RepositoryState.Loading(this.data?.let { transform(it) })
        }

        is RepositoryState.Error -> {
            RepositoryState.Error(this.error)
        }

        is RepositoryState.Data -> {
            val transformedData = transform(this.data)
            if (transformedData == null) {
                RepositoryState.Empty
            } else {
                RepositoryState.Data(transformedData)
            }
        }
    }
}

fun <T1, T2, E, R> combineStates(
    flow1: Flow<RepositoryState<T1, E>>,
    flow2: Flow<RepositoryState<T2, E>>,
    transform: suspend (a: T1, b: T2) -> R
): Flow<RepositoryState<R, E>> {
    return combine(flow1, flow2) { value1, value2 ->
        when {
            value1 is RepositoryState.Error -> RepositoryState.Error(value1.error)
            value2 is RepositoryState.Error -> RepositoryState.Error(value2.error)

            value1 is RepositoryState.Loading || value2 is RepositoryState.Loading -> {
                val data1 = value1.dataOrNull()
                val data2 = value2.dataOrNull()
                if (data1 != null && data2 != null) {
                    RepositoryState.Loading(transform(data1, data2))
                } else {
                    RepositoryState.Loading(data = null)
                }
            }

            value1 is RepositoryState.Data && value2 is RepositoryState.Data -> {
                val result = transform(value1.data, value2.data)
                RepositoryState.Data(result)
            }

            value1 is RepositoryState.Empty || value2 is RepositoryState.Empty -> RepositoryState.Empty

            else -> RepositoryState.Empty
        }
    }
}