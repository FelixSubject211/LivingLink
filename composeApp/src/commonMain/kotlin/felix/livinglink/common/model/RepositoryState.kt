package felix.livinglink.common.model

import kotlinx.coroutines.flow.Flow
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