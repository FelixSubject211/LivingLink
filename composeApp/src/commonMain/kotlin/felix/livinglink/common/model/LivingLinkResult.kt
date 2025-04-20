package felix.livinglink.common.model

sealed class LivingLinkResult<out DATA, out ERROR> {
    data class Data<DATA>(val data: DATA) : LivingLinkResult<DATA, Nothing>()
    data class Error<ERROR>(val error: ERROR) : LivingLinkResult<Nothing, ERROR>()
}

fun <DATA, NEW_DATA, ERROR> LivingLinkResult<DATA, ERROR>.map(
    transform: (DATA) -> NEW_DATA
): LivingLinkResult<NEW_DATA, ERROR> {
    return when (this) {
        is LivingLinkResult.Error<*> -> {
            this
        }

        is LivingLinkResult.Data<DATA> -> {
            LivingLinkResult.Data(transform(this.data))
        }
    }
}