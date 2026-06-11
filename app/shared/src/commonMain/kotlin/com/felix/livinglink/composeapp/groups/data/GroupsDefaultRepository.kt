package com.felix.livinglink.composeapp.groups.data

import com.felix.livinglink.composeapp.auth.domain.AuthRepository
import com.felix.livinglink.composeapp.auth.domain.AuthState
import com.felix.livinglink.composeapp.core.domain.NetworkResult
import com.felix.livinglink.composeapp.core.domain.Loadable
import com.felix.livinglink.composeapp.groups.domain.Group
import com.felix.livinglink.composeapp.groups.domain.GroupsContent
import com.felix.livinglink.composeapp.groups.domain.GroupsRemoteDataSource
import com.felix.livinglink.composeapp.groups.domain.GroupsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single

@Single(binds = [GroupsRepository::class])
class GroupsDefaultRepository(
    private val groupsRemoteDataSource: GroupsRemoteDataSource,
    private val authRepository: AuthRepository,
) : GroupsRepository {

    private val selectedGroupId = MutableStateFlow<String?>(null)

    private val loadResult: Flow<LoadResult> =
        flow {
            emit(LoadResult.Loading)
            emit(loadOnce())
        }

    override val state: Flow<Loadable<GroupsContent>> =
        combine(loadResult, selectedGroupId) { result, selectedId ->
            result.toState(selectedId)
        }

    override fun selectGroup(groupId: String) {
        selectedGroupId.value = groupId
    }

    private suspend fun loadOnce(): LoadResult {
        val apiKey =
            (authRepository.authState.value as? AuthState.LoggedIn)?.apiKey
                ?: return LoadResult.Loading

        return when (val result = groupsRemoteDataSource.getGroups(apiKey)) {
            is NetworkResult.Success -> {
                LoadResult.Loaded(result.value)
            }
            is NetworkResult.NetworkError -> {
                LoadResult.Error(Loadable.Error.Network)
            }
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
}