package felix.projekt.livinglink.composeApp.core.domain

sealed class Result<out DATA, out ERROR> {
    data class Success<DATA>(val data: DATA) : Result<DATA, Nothing>()
    data class Error<ERROR>(val error: ERROR) : Result<Nothing, ERROR>()
}

fun <DATA, DATA_NEW, ERROR> Result<DATA, ERROR>.map(
    transform: (DATA) -> DATA_NEW
): Result<DATA_NEW, ERROR> = when (this) {
    is Result.Success<DATA> -> {
        Result.Success(transform(this.data))
    }

    is Result.Error<*> -> {
        this
    }
}