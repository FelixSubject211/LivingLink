package felix.livinglink.ui.common.state

import felix.livinglink.common.model.LivingLinkError
import felix.livinglink.common.model.LivingLinkResult
import felix.livinglink.common.model.RepositoryState
import felix.livinglink.haptics.controller.HapticsController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapNotNull
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
        assert: (currentData: DATA) -> LivingLinkResult<Unit, ERROR> = {
            LivingLinkResult.Success(Unit)
        },
        request: suspend (currentData: DATA) -> LivingLinkResult<RESULT, REQUEST_ERROR>,
        onSuccess: (
            currentData: DATA,
            result: RESULT
        ) -> LivingLinkResult<DATA, ERROR> = { data, _ ->
            LivingLinkResult.Success(data)
        }
    )

    fun cancel()

    sealed class State<LOADABLE_DATA, LOADABLE_ERROR> {
        class Empty<LOADABLE_DATA, LOADABLE_ERROR> : State<LOADABLE_DATA, LOADABLE_ERROR>()
        class Loading<LOADABLE_DATA, LOADABLE_ERROR> : State<LOADABLE_DATA, LOADABLE_ERROR>()
        data class Data<LOADABLE_DATA, LOADABLE_ERROR>(val data: LOADABLE_DATA) :
            State<LOADABLE_DATA, LOADABLE_ERROR>()

        fun <LOADABLE_DATA1, LOADABLE_DATA2, LOADABLE_ERROR> mapState(
            transform: (LOADABLE_DATA1) -> LOADABLE_DATA2
        ): State<LOADABLE_DATA2, LOADABLE_ERROR> {
            return when (this) {
                is Empty<*, *> -> {
                    Empty()
                }

                is Loading<*, *> -> {
                    Loading()
                }

                is Data<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    val transformedData = transform(this.data as LOADABLE_DATA1)
                    Data(transformedData)
                }
            }
        }

        fun dataOrNull(): LOADABLE_DATA? {
            @Suppress("UNCHECKED_CAST")
            return when (this) {
                is Empty<*, *> -> null
                is Loading<*, *> -> null
                is Data<*, *> -> this.data as LOADABLE_DATA
            }
        }
    }

    sealed class CombinedError<out LOADABLE_ERROR, out ERROR, out REQUEST_ERROR> : LivingLinkError {
        abstract val value: LivingLinkError

        data class Loadable<LOADABLE_ERROR : LivingLinkError>(override val value: LOADABLE_ERROR) :
            CombinedError<LOADABLE_ERROR, Nothing, Nothing>() {
            override fun title() = value.title()
            override fun message() = value.message()
        }

        data class Error<ERROR : LivingLinkError>(override val value: ERROR) :
            CombinedError<Nothing, ERROR, Nothing>() {
            override fun title() = value.title()
            override fun message() = value.message()
        }

        data class Request<REQUEST_ERROR : LivingLinkError>(override val value: REQUEST_ERROR) :
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
    private val job = Job(scope.coroutineContext[Job])
    private val internalScope = CoroutineScope(context = scope.coroutineContext + job)

    private val _error = MutableStateFlow<LOADABLE_ERROR?>(null)
    private val _loading = MutableStateFlow(false)

    private val viewModelState = ViewModelDefaultState<DATA, ERROR, REQUEST_ERROR>(
        initialState = initialState,
        hapticsController = hapticsController,
        scope = scope
    )

    override val loadableData = input.mapNotNull { repoState ->
        when (repoState) {
            RepositoryState.Empty -> {
                _loading.value = false
                LoadableViewModelState.State.Empty()
            }

            is RepositoryState.Loading<LOADABLE_DATA> -> {
                val data = repoState.data
                if (data == null) {
                    LoadableViewModelState.State.Loading()
                } else {
                    _loading.value = true
                    LoadableViewModelState.State.Data<LOADABLE_DATA, LOADABLE_ERROR>(data)
                }
            }

            is RepositoryState.Error<LOADABLE_ERROR> -> {
                _loading.value = false
                _error.value = repoState.error
                null
            }

            is RepositoryState.Data<LOADABLE_DATA, LOADABLE_ERROR> -> {
                _loading.value = false
                LoadableViewModelState.State.Data(repoState.data)
            }
        }
    }.stateIn(
        scope = internalScope,
        started = SharingStarted.Lazily,
        initialValue = LoadableViewModelState.State.Loading()
    )

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
            scope = internalScope,
            started = SharingStarted.Lazily,
            initialValue = null
        )

    override val loading: StateFlow<Boolean> =
        viewModelState.loading
            .combine(_loading) { viewModelLoading, loadableLoading ->
                viewModelLoading || loadableLoading
            }
            .stateIn(
                scope = internalScope,
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
        assert: (currentData: DATA) -> LivingLinkResult<Unit, ERROR>,
        request: suspend (currentData: DATA) -> LivingLinkResult<RESULT, REQUEST_ERROR>,
        onSuccess: (currentData: DATA, result: RESULT) -> LivingLinkResult<DATA, ERROR>
    ) = viewModelState.perform(assert = assert, request = request, onSuccess = onSuccess)

    override fun perform(action: (DATA) -> DATA) = viewModelState.perform(action = action)

    override fun cancel() {
        viewModelState.cancel()
        job.cancel()
    }
}