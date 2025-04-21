package felix.livinglink.ui.common.state

import felix.livinglink.common.model.LivingLinkError
import felix.livinglink.common.model.LivingLinkResult
import felix.livinglink.haptics.controller.HapticsController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

interface ViewModelState<DATA, ERROR : LivingLinkError, REQUEST_ERROR : LivingLinkError> {
    val data: StateFlow<DATA>
    val error: StateFlow<CombinedError<ERROR, REQUEST_ERROR>?>
    val loading: StateFlow<Boolean>

    fun closeError()

    fun perform(action: (DATA) -> DATA)

    fun <RESULT> perform(
        assert: (currentData: DATA) -> LivingLinkResult<Unit, ERROR> = {
            LivingLinkResult.Data(Unit)
        },
        request: suspend (currentData: DATA) -> LivingLinkResult<RESULT, REQUEST_ERROR>,
        onSuccess: (
            currentData: DATA,
            result: RESULT
        ) -> LivingLinkResult<DATA, ERROR> = { data, _ ->
            LivingLinkResult.Data(data)
        }
    )

    sealed class CombinedError<out ERROR, out REQUEST_ERROR> : LivingLinkError {
        abstract val value: LivingLinkError

        data class Error<ERROR : LivingLinkError>(override val value: ERROR) :
            CombinedError<ERROR, Nothing>() {
            override fun title() = value.title()
            override fun message() = value.message()
        }

        data class Request<REQUEST_ERROR : LivingLinkError>(override val value: REQUEST_ERROR) :
            CombinedError<Nothing, REQUEST_ERROR>() {
            override fun title() = value.title()
            override fun message() = value.message()
        }
    }
}

class ViewModelDefaultState<DATA, ERROR : LivingLinkError, REQUEST_ERROR : LivingLinkError>(
    val initialState: DATA,
    private val hapticsController: HapticsController,
    private val scope: CoroutineScope,
) : ViewModelState<DATA, ERROR, REQUEST_ERROR> {
    private val taskCount = MutableStateFlow(0)

    override val data = MutableStateFlow(initialState)

    override val error = MutableStateFlow<ViewModelState.CombinedError<ERROR, REQUEST_ERROR>?>(
        null
    )

    @OptIn(FlowPreview::class)
    override val loading = taskCount.map { taskCount ->
        taskCount > 0
    }.debounce(timeoutMillis = 200).stateIn(
        scope = scope,
        started = SharingStarted.Lazily,
        initialValue = false
    )

    override fun closeError() {
        error.update { null }
    }

    override fun perform(action: (DATA) -> DATA) {
        if (loading.value) {
            return
        }
        data.update(action)
    }

    override fun <RESULT> perform(
        assert: (currentData: DATA) -> LivingLinkResult<Unit, ERROR>,
        request: suspend (currentData: DATA) -> LivingLinkResult<RESULT, REQUEST_ERROR>,
        onSuccess: (currentData: DATA, result: RESULT) -> LivingLinkResult<DATA, ERROR>
    ) {
        if (loading.value) {
            return
        }

        when (val assertResult = assert(data.value)) {
            is LivingLinkResult.Error -> {
                error.value = ViewModelState.CombinedError.Error(assertResult.error)
                return
            }

            else -> {}
        }

        scope.launch {
            taskCount.update { it + 1 }

            hapticsController.performLightImpact()
            when (val response = request(data.value)) {
                is LivingLinkResult.Error -> {
                    hapticsController.performError()
                    error.value = ViewModelState.CombinedError.Request(response.error)
                }

                is LivingLinkResult.Data -> {
                    when (val result = onSuccess(data.value, response.data)) {
                        is LivingLinkResult.Data -> {
                            hapticsController.performSuccess()
                            data.update { result.data }
                        }

                        is LivingLinkResult.Error -> {
                            hapticsController.performError()
                            error.value = ViewModelState.CombinedError.Error(result.error)
                        }
                    }
                }
            }

            taskCount.update { it - 1 }
        }
    }
}