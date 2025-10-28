package felix.projekt.livinglink.composeApp.groups.domain

import felix.projekt.livinglink.composeApp.core.domain.NetworkError
import felix.projekt.livinglink.composeApp.core.domain.Result
import kotlinx.coroutines.flow.Flow

interface GroupsRepository {
    val getGroups: Flow<GroupsRepositoryState>
    suspend fun createGroup(groupName: String): Result<CreateGroupResponse, NetworkError>

    sealed class GroupsRepositoryState {
        data object Loading : GroupsRepositoryState()
        data class Data(val groups: Map<String, Group>) : GroupsRepositoryState()
    }
}