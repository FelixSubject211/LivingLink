package felix.livinglink.ui.common.state

import felix.livinglink.common.model.LivingLinkError
import felix.livinglink.common.model.LivingLinkResult
import felix.livinglink.common.model.RepositoryState
import felix.livinglink.haptics.HapticsController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

interface LoadableViewModelState<
        LOADABLE_DATA,
        DATA,
        LOADABLE_ERROR : LivingLinkError,
        ERROR : LivingLinkError,
        REQUEST_ERROR : LivingLinkError
        > {
    val loadableData: StateFlow<State<LOADABLE_DATA, LOADABLE_ERROR>>

    val data: StateFlow<DATA>
    val error: StateFlow<CombinedError<LOADABLE_ERROR, ERROR, REQUEST_ERROR>?>
    val loading: StateFlow<Boolean>

    fun closeError()

    fun perform(action: (DATA) -> DATA)

    fun <RESULT> perform(
        request: suspend (currentData: DATA) -> LivingLinkResult<RESULT, REQUEST_ERROR>,
        onSuccess: (
            currentData: DATA,
            result: RESULT
        ) -> LivingLinkResult<DATA, ERROR> = { data, _ ->
            LivingLinkResult.Data(data)
        }
    )

    sealed class State<LOADABLE_DATA, LOADABLE_ERROR> {
        class Empty<LOADABLE_DATA, LOADABLE_ERROR> : State<LOADABLE_DATA, LOADABLE_ERROR>()
        class Loading<LOADABLE_DATA, LOADABLE_ERROR> : State<LOADABLE_DATA, LOADABLE_ERROR>()
        data class Error<LOADABLE_DATA, LOADABLE_ERROR>(val error: LOADABLE_ERROR) :
            State<LOADABLE_DATA, LOADABLE_ERROR>()

        data class Data<LOADABLE_DATA, LOADABLE_ERROR>(val data: LOADABLE_DATA) :
            State<LOADABLE_DATA, LOADABLE_ERROR>()
    }

    sealed class CombinedError<out LOADABLE_ERROR, out ERROR, out REQUEST_ERROR> : LivingLinkError {
        data class Loadable<LOADABLE_ERROR : LivingLinkError>(val value: LOADABLE_ERROR) :
            CombinedError<LOADABLE_ERROR, Nothing, Nothing>() {
            override fun title() = value.title()
            override fun message() = value.message()
        }

        data class Error<ERROR : LivingLinkError>(val value: ERROR) :
            CombinedError<Nothing, ERROR, Nothing>() {
            override fun title() = value.title()
            override fun message() = value.message()
        }

        data class Request<REQUEST_ERROR : LivingLinkError>(val value: REQUEST_ERROR) :
            CombinedError<Nothing, Nothing, REQUEST_ERROR>() {
            override fun title() = value.title()
            override fun message() = value.message()
        }
    }
}

class LoadableViewModelDefaultState<
        LOADABLE_DATA,
        DATA,
        LOADABLE_ERROR : LivingLinkError,
        ERROR : LivingLinkError,
        REQUEST_ERROR : LivingLinkError
        >(
    input: Flow<RepositoryState<LOADABLE_DATA, LOADABLE_ERROR>>,
    initialState: DATA,
    hapticsController: HapticsController,
    scope: CoroutineScope,
) : LoadableViewModelState<LOADABLE_DATA, DATA, LOADABLE_ERROR, ERROR, REQUEST_ERROR> {

    private val _error = MutableStateFlow<LOADABLE_ERROR?>(null)
    private val _loading = MutableStateFlow(false)

    private val viewModelState = ViewModelDefaultState<DATA, ERROR, REQUEST_ERROR>(
        initialState = initialState,
        hapticsController = hapticsController,
        scope = scope
    )

    @Suppress("UNCHECKED_CAST")
    override val loadableData = input.map { state ->
        when (state) {
            RepositoryState.Empty -> {
                _loading.value = false
                LoadableViewModelState.State.Empty<LOADABLE_DATA, LOADABLE_ERROR>()
            }

            is RepositoryState.Loading<*> -> {
                if (state.data == null) {
                    LoadableViewModelState.State.Loading<LOADABLE_DATA, LOADABLE_ERROR>()
                } else {
                    _loading.value = true
                    LoadableViewModelState.State.Data(state.data)
                }
            }

            is RepositoryState.Error -> {
                val error = state.error
                if (state.data == null) {
                    LoadableViewModelState.State.Error(error)
                } else {
                    _error.value = error
                    LoadableViewModelState.State.Data(state.data)
                }
            }

            is RepositoryState.Data -> {
                _loading.value = false
                LoadableViewModelState.State.Data(state.data)
            }
        }
    }.stateIn(
        scope = scope,
        started = SharingStarted.Lazily,
        initialValue = LoadableViewModelState.State.Loading<LOADABLE_DATA, LOADABLE_ERROR>()
    ) as StateFlow<LoadableViewModelState.State<LOADABLE_DATA, LOADABLE_ERROR>>

    override val data: StateFlow<DATA> = viewModelState.data


    override val error = viewModelState.error
        .combine(_error) { viewModelError, loadableError ->
            when (viewModelError) {
                is ViewModelState.CombinedError.Error ->
                    LoadableViewModelState.CombinedError.Error(viewModelError.value)

                is ViewModelState.CombinedError.Request ->
                    LoadableViewModelState.CombinedError.Request(viewModelError.value)

                null -> loadableError?.let {
                    LoadableViewModelState.CombinedError.Loadable(it)
                }
            }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Lazily,
            initialValue = null
        )

    override val loading: StateFlow<Boolean> =
        viewModelState.loading
            .combine(_loading) { viewModelLoading, loadableLoading ->
                viewModelLoading || loadableLoading
            }
            .stateIn(
                scope = scope,
                started = SharingStarted.Lazily,
                initialValue = false
            )

    override fun closeError() {
        _error.value?.let {
            _error.value = null
        } ?: run {
            viewModelState.closeError()
        }
    }

    override fun <RESULT> perform(
        request: suspend (currentData: DATA) -> LivingLinkResult<RESULT, REQUEST_ERROR>,
        onSuccess: (currentData: DATA, result: RESULT) -> LivingLinkResult<DATA, ERROR>
    ) = viewModelState.perform(request = request, onSuccess = onSuccess)

    override fun perform(action: (DATA) -> DATA) = viewModelState.perform(action = action)
}