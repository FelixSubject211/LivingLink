package felix.livinglink.common.repository

import felix.livinglink.common.model.LivingLinkError
import felix.livinglink.common.model.LivingLinkResult
import felix.livinglink.common.model.RepositoryState
import felix.livinglink.common.model.dataOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

interface FetchAndStoreDataHandler<DATA, ERROR : LivingLinkError> {
    operator fun invoke(
        loadFromDb: suspend () -> Flow<List<DATA>>,
        networkRequest: suspend () -> LivingLinkResult<List<DATA>, ERROR>,
        saveToDb: suspend (List<DATA>) -> Unit,
        events: Flow<FetchAndStoreDataEvent>
    ): Flow<RepositoryState<List<DATA>, ERROR>>
}

class FetchAndStoreDataDefaultHandler<DATA, ERROR : LivingLinkError>(
    private val scope: CoroutineScope
) : FetchAndStoreDataHandler<DATA, ERROR> {
    override fun invoke(
        loadFromDb: suspend () -> Flow<List<DATA>>,
        networkRequest: suspend () -> LivingLinkResult<List<DATA>, ERROR>,
        saveToDb: suspend (List<DATA>) -> Unit,
        events: Flow<FetchAndStoreDataEvent>
    ): Flow<RepositoryState<List<DATA>, ERROR>> {

        val stateFlow = MutableStateFlow<RepositoryState<List<DATA>, ERROR>>(
            RepositoryState.Loading(null)
        )

        scope.launch {
            val initialData = loadFromDb().firstOrNull()
            if (initialData?.isEmpty() == true || initialData == null) {
                stateFlow.value = RepositoryState.Empty(null)
            } else {
                stateFlow.value = RepositoryState.Data(initialData)
            }
        }

        scope.launch {
            events.collect { event ->
                when (event) {
                    FetchAndStoreDataEvent.CLEAR -> {
                        stateFlow.value = RepositoryState.Empty(null)
                        saveToDb(emptyList())
                    }

                    FetchAndStoreDataEvent.RELOAD -> {
                        stateFlow.value = RepositoryState.Loading(stateFlow.value.dataOrNull())

                        when (val result = networkRequest()) {
                            is LivingLinkResult.Success<List<DATA>> -> {
                                saveToDb(result.data)
                                stateFlow.value = if (result.data.isEmpty()) {
                                    RepositoryState.Empty(null)
                                } else {
                                    RepositoryState.Data(result.data)
                                }
                            }

                            is LivingLinkResult.Error<ERROR> -> {
                                stateFlow.value = RepositoryState.Error(error = result.error)
                                if (stateFlow.value.dataOrNull()?.isEmpty() == true) {
                                    stateFlow.value = RepositoryState.Empty(
                                        data = stateFlow.value.dataOrNull()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        return stateFlow.asStateFlow()
    }
}

enum class FetchAndStoreDataEvent {
    RELOAD,
    CLEAR
}