package felix.livinglink.common.model

sealed class LivingLinkResult<out DATA, out ERROR> {
    data class Data<DATA>(val data: DATA) : LivingLinkResult<DATA, Nothing>()
    data class Error<ERROR>(val error: ERROR) : LivingLinkResult<Nothing, ERROR>()
}