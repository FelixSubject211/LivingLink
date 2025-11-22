package felix.projekt.livinglink.composeApp.eventSourcing.interfaces

import kotlinx.serialization.json.JsonElement

interface AppendEventService {

    suspend operator fun <TTopic : EventTopic, TState, R> invoke(
        aggregator: Aggregator<TTopic, TState>,
        maxRetries: Int = 30,
        buildEvent: (TState) -> OperationResult<R>
    ): FinalResult<R>

    sealed class OperationResult<out R> {
        data class EmitEvent<R>(val payload: JsonElement, val response: R) : OperationResult<R>()
        data class NoOperation<R>(val response: R) : OperationResult<R>()
    }

    sealed class FinalResult<out R> {
        data class Success<R>(val response: R) : FinalResult<R>()
        data class NoOperation<R>(val response: R) : FinalResult<R>()
        data class NotAuthorized<R>(val response: R? = null) : FinalResult<R>()
        data class NetworkError<R>(val response: R? = null) : FinalResult<R>()
        data class VersionMismatch<R>(val response: R? = null) : FinalResult<R>()
    }
}