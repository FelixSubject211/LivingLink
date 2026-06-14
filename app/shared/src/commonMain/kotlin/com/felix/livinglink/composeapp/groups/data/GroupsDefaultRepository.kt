package com.felix.livinglink.composeapp.groups.data

import com.felix.livinglink.composeapp.auth.domain.AuthRepository
import com.felix.livinglink.composeapp.auth.domain.AuthState
import com.felix.livinglink.composeapp.core.domain.NetworkResult
import com.felix.livinglink.composeapp.core.domain.Loadable
import com.felix.livinglink.composeapp.groups.domain.Group
import com.felix.livinglink.composeapp.groups.domain.GroupsContent
import com.felix.livinglink.composeapp.groups.domain.GroupsRemoteDataSource
import com.felix.livinglink.composeapp.groups.domain.GroupsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import org.koin.core.annotation.Single
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Single(binds = [GroupsRepository::class])
class GroupsDefaultRepository(
    private val groupsRemoteDataSource: GroupsRemoteDataSource,
    private val authRepository: AuthRepository,
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : GroupsRepository {

    private val _selectedGroupId = MutableStateFlow<String?>(null)

    private val lastLoaded = MutableStateFlow<List<Group>?>(null)

    private val loadResult: Flow<LoadResult> =
        flow {
            emit(LoadResult.Loading)
            while (true) {
                val result = loadOnce()
                emit(result)
                val interval = if (result is LoadResult.Error) RETRY_INTERVAL else POLL_INTERVAL
                delay(interval)
            }
        }.shareIn(scope, SharingStarted.WhileSubscribed(), replay = 1)

    override val state: Flow<Loadable<GroupsContent>> =
        combine(loadResult, _selectedGroupId) { result, selectedId ->
            result.toState(selectedId)
        }

    override val selectedGroupId: Flow<String?> =
        state.map { loadable ->
            (loadable as? Loadable.Content)?.value?.selectedGroup?.id
        }

    override fun selectGroup(groupId: String) {
        _selectedGroupId.value = groupId
    }

    private suspend fun loadOnce(): LoadResult {
        val apiKey =
            (authRepository.authState.value as? AuthState.LoggedIn)?.apiKey
                ?: return LoadResult.Loading

        return when (val result = groupsRemoteDataSource.getGroups(apiKey)) {
            is NetworkResult.Success -> {
                lastLoaded.value = result.value
                LoadResult.Loaded(result.value)
            }
            is NetworkResult.NetworkError ->
                lastLoaded.value?.let { LoadResult.Loaded(it) }
                    ?: LoadResult.Error(Loadable.Error.Network)
            is NetworkResult.Unauthorized -> {
                authRepository.clear()
                LoadResult.Loading
            }
        }
    }

    private sealed interface LoadResult {
        data object Loading : LoadResult

        data class Loaded(
            val groups: List<Group>,
        ) : LoadResult

        data class Error(
            val error: Loadable.Error,
        ) : LoadResult

        fun toState(selectedId: String?): Loadable<GroupsContent> =
            when (this) {
                is Loading -> Loadable.Loading
                is Error -> error
                is Loaded ->
                    when {
                        groups.isEmpty() -> Loadable.Empty
                        else -> {
                            val selected =
                                groups.firstOrNull { it.id == selectedId }
                                    ?: groups.first()
                            Loadable.Content(
                                GroupsContent(groups = groups, selectedGroup = selected)
                            )
                        }
                    }
            }
    }

    private companion object {
        val POLL_INTERVAL = 1.minutes
        val RETRY_INTERVAL = 1.seconds
    }
}