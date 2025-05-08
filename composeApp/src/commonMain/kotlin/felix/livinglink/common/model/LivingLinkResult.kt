package felix.livinglink.common.model

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

sealed class LivingLinkResult<out DATA, out ERROR> {
    data class Success<DATA>(val data: DATA) : LivingLinkResult<DATA, Nothing>()
    data class Error<ERROR>(val error: ERROR) : LivingLinkResult<Nothing, ERROR>()
}

fun <DATA, NEW_DATA, ERROR> LivingLinkResult<DATA, ERROR>.map(
    transform: (DATA) -> NEW_DATA
): LivingLinkResult<NEW_DATA, ERROR> {
    return when (this) {
        is LivingLinkResult.Error<*> -> {
            this
        }

        is LivingLinkResult.Success<DATA> -> {
            LivingLinkResult.Success(transform(this.data))
        }
    }
}

@OptIn(ExperimentalContracts::class)
inline fun <DATA, ERROR> LivingLinkResult<DATA, ERROR>.alsoIfIsSuccess(
    block: (LivingLinkResult<DATA, ERROR>) -> Unit
): LivingLinkResult<DATA, ERROR> {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    when(this) {
        is LivingLinkResult.Success<*> -> {
            block(this)
        }
        is LivingLinkResult.Error<*> -> {}
    }

    return this
}