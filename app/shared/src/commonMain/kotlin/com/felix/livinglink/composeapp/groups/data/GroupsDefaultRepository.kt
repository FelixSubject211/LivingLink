package com.felix.livinglink.composeapp.groups.data

import com.felix.livinglink.composeapp.auth.domain.AuthRepository
import com.felix.livinglink.composeapp.auth.domain.AuthState
import com.felix.livinglink.composeapp.core.domain.Loadable
import com.felix.livinglink.composeapp.core.domain.NetworkResult
import com.felix.livinglink.composeapp.groups.domain.GroupsContent
import com.felix.livinglink.composeapp.groups.domain.GroupsLocalDataSource
import com.felix.livinglink.composeapp.groups.domain.GroupsRemoteDataSource
import com.felix.livinglink.composeapp.groups.domain.GroupsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Single(binds = [GroupsRepository::class])
class GroupsDefaultRepository(
    private val groupsRemoteDataSource: GroupsRemoteDataSource,
    private val groupsLocalDataSource: GroupsLocalDataSource,
    private val authRepository: AuthRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : GroupsRepository {

    private val refreshError: Flow<Loadable.Error?> =
        flow {
            emit(null)
            while (true) {
                val error = refreshOnce()
                emit(error)
                val interval = if (error != null) RETRY_INTERVAL else POLL_INTERVAL
                delay(interval)
            }
        }.shareIn(scope, SharingStarted.WhileSubscribed(replayExpirationMillis = 0), replay = 1)

    override val state: Flow<Loadable<GroupsContent>> =
        combine(
            groupsLocalDataSource.observe(),
            groupsLocalDataSource.observeSelectedGroupId(),
            refreshError,
        ) { cachedGroups, selectedId, error ->
            when {
                cachedGroups != null && cachedGroups.isNotEmpty() -> {
                    val selected =
                        cachedGroups.firstOrNull { it.id == selectedId }
                            ?: cachedGroups.first()
                    Loadable.Content(
                        GroupsContent(groups = cachedGroups, selectedGroup = selected)
                    )
                }

                cachedGroups != null -> Loadable.Empty

                error != null -> error

                else -> Loadable.Loading
            }
        }

    override fun selectGroup(groupId: String) {
        scope.launch { groupsLocalDataSource.setSelectedGroupId(groupId) }
    }

    override val selectedGroupId: Flow<String?> =
        state.map { loadable ->
            (loadable as? Loadable.Content)?.value?.selectedGroup?.id
        }

    private suspend fun refreshOnce(): Loadable.Error? {
        val apiKey =
            (authRepository.authState.value as? AuthState.LoggedIn)?.apiKey
                ?: return null

        return when (val result = groupsRemoteDataSource.getGroups(apiKey)) {
            is NetworkResult.Success -> {
                groupsLocalDataSource.replaceAll(result.value)
                null
            }

            is NetworkResult.NetworkError ->
                Loadable.Error.Network

            is NetworkResult.Unauthorized -> {
                authRepository.clear()
                null
            }
        }
    }

    private companion object {
        val POLL_INTERVAL = 1.minutes
        val RETRY_INTERVAL = 1.seconds
    }
}