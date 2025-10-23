package felix.projekt.livinglink.composeApp.groups.domain

import felix.projekt.livinglink.composeApp.core.domain.NetworkError
import felix.projekt.livinglink.composeApp.core.domain.Result
import kotlinx.coroutines.flow.Flow

interface GroupsRepository {
    val getGroups: Flow<GroupRepositoryState>
    suspend fun createGroup(groupName: String): Result<CreateGroupResponse, NetworkError>

    sealed class GroupRepositoryState {
        data object Loading : GroupRepositoryState()
        data class Data(val groups: List<Group>) : GroupRepositoryState()
    }
}