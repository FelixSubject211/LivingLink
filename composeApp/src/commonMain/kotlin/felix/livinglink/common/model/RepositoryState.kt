package felix.livinglink.common.model

sealed class RepositoryState<out DATA, out ERROR> {
    data object Empty : RepositoryState<Nothing, Nothing>()
    data class Loading<out DATA>(val data: DATA?) : RepositoryState<DATA, Nothing>()
    data class Error<out DATA, out ERROR>(val data: DATA?, val error: ERROR) :
        RepositoryState<DATA, ERROR>()

    data class Data<out DATA, out ERROR>(val data: DATA) : RepositoryState<DATA, ERROR>()
}