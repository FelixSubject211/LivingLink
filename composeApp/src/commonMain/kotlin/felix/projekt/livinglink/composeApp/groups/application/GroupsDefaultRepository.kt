package felix.projekt.livinglink.composeApp.groups.application

import felix.projekt.livinglink.composeApp.AppConfig
import felix.projekt.livinglink.composeApp.auth.interfaces.GetAuthStateService
import felix.projekt.livinglink.composeApp.core.domain.NetworkError
import felix.projekt.livinglink.composeApp.core.domain.Result
import felix.projekt.livinglink.composeApp.groups.domain.CreateGroupResponse
import felix.projekt.livinglink.composeApp.groups.domain.GetGroupsResponse
import felix.projekt.livinglink.composeApp.groups.domain.Group
import felix.projekt.livinglink.composeApp.groups.domain.GroupsNetworkDataSource
import felix.projekt.livinglink.composeApp.groups.domain.GroupsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.select
import kotlin.concurrent.atomics.ExperimentalAtomicApi

class GroupsDefaultRepository(
    private val groupsNetworkDataSource: GroupsNetworkDataSource,
    private val getAuthStateService: GetAuthStateService,
    private val scope: CoroutineScope
) : GroupsRepository {
    private val manualUpdateChannel = Channel<ManualUpdateItem>(Channel.CONFLATED)
    private val logoutChannel = Channel<Unit>(Channel.UNLIMITED)
    private val loginChannel = Channel<Unit>(Channel.UNLIMITED)
    private var currentGroups = MutableStateFlow<Map<String, Group>>(emptyMap())
    private var hasWaitedForLogin = false

    sealed class ManualUpdateItem {
        data class AddGroup(val group: Group) : ManualUpdateItem()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val pollingFlow: Flow<Map<String, Group>?> = pollGroups().also {
        launchAuthCollector()
    }

    override val getGroups: StateFlow<GroupsRepository.GroupRepositoryState> = pollingFlow
        .map { groups ->
            groups?.let {
                GroupsRepository.GroupRepositoryState.Data(groups = it.values.toList())
            } ?: GroupsRepository.GroupRepositoryState.Loading
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = GroupsRepository.GroupRepositoryState.Loading
        )

    override suspend fun createGroup(groupName: String): Result<CreateGroupResponse, NetworkError> {
        return groupsNetworkDataSource.createGroup(groupName).also { result ->
            if (result is Result.Success && result.data is CreateGroupResponse.Success) {
                manualUpdateChannel.send(ManualUpdateItem.AddGroup(result.data.group))
            }
        }
    }

    private fun launchAuthCollector() {
        scope.launch {
            getAuthStateService().collect { authState ->
                when (authState) {
                    GetAuthStateService.AuthState.LoggedOut -> logoutChannel.send(Unit)
                    GetAuthStateService.AuthState.LoggedIn -> loginChannel.send(Unit)
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, ExperimentalAtomicApi::class)
    private fun pollGroups(): Flow<Map<String, Group>?> = flow {
        if (!hasWaitedForLogin) {
            loginChannel.receive()
            hasWaitedForLogin = true
        }

        while (scope.isActive) {
            val currentGroupVersions = currentGroups.value.mapValues { it.value.version }
            val result = groupsNetworkDataSource.getGroups(currentGroupVersions)
            val nextPollDelay = when (result) {
                is Result.Success -> {
                    when (val data = result.data) {
                        is GetGroupsResponse.Success -> {
                            currentGroups.update { data.groups }
                            emit(data.groups)
                            data.nextPollAfterMillis
                        }

                        is GetGroupsResponse.NotModified -> {
                            emit(currentGroups.value)
                            data.nextPollAfterMillis
                        }
                    }
                }

                is Result.Error -> AppConfig.groupsPollFallbackMills
            }
            select {
                onTimeout(nextPollDelay) {}
                logoutChannel.onReceive {
                    currentGroups.update { emptyMap() }
                    emit(null)
                    select {
                        loginChannel.onReceive {}
                    }
                }
                manualUpdateChannel.onReceive { item ->
                    handleManualUpdate(item)
                    emit(currentGroups.value)
                }
            }
        }
    }

    private fun handleManualUpdate(item: ManualUpdateItem) {
        when (item) {
            is ManualUpdateItem.AddGroup -> {
                currentGroups.update { current ->
                    current + (item.group.id to item.group)
                }
            }
        }
    }
}