package felix.livinglink.groups.repository

import felix.livinglink.common.model.LivingLinkResult
import felix.livinglink.common.model.RepositoryState
import felix.livinglink.common.model.alsoIfIsSuccess
import felix.livinglink.common.model.dataOrNull
import felix.livinglink.common.model.map
import felix.livinglink.common.model.mapState
import felix.livinglink.common.network.NetworkError
import felix.livinglink.common.repository.FetchAndStoreDataDefaultHandler
import felix.livinglink.common.repository.FetchAndStoreDataEvent
import felix.livinglink.event.eventbus.EventBus
import felix.livinglink.group.CreateGroupRequest
import felix.livinglink.group.CreateGroupResponse
import felix.livinglink.group.CreateInviteRequest
import felix.livinglink.group.CreateInviteResponse
import felix.livinglink.group.DeleteGroupResponse
import felix.livinglink.group.Group
import felix.livinglink.group.LeaveGroupRequest
import felix.livinglink.group.LeaveGroupResponse
import felix.livinglink.group.MakeUserAdminRequest
import felix.livinglink.group.MakeUserAdminResponse
import felix.livinglink.group.RemoveUserFromGroupRequest
import felix.livinglink.group.RemoveUserFromGroupResponse
import felix.livinglink.group.UseInviteRequest
import felix.livinglink.group.UseInviteResponse
import felix.livinglink.groups.network.GroupsNetworkDataSource
import felix.livinglink.groups.store.GroupStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn

interface GroupsRepository {
    val groups: Flow<RepositoryState<List<Group>, NetworkError>>

    fun group(groupId: String): Flow<RepositoryState<Group, NetworkError>>

    fun resolveUserName(groupId: String, userId: String): Flow<String?>

    suspend fun createGroup(
        request: CreateGroupRequest
    ): LivingLinkResult<CreateGroupResponse, NetworkError>

    suspend fun deleteGroup(
        groupId: String
    ): LivingLinkResult<DeleteGroupResponse, NetworkError>

    suspend fun leaveGroup(
        groupId: String
    ): LivingLinkResult<LeaveGroupResponse, NetworkError>

    suspend fun createInvite(
        request: CreateInviteRequest
    ): LivingLinkResult<CreateInviteResponse, NetworkError>

    suspend fun useInvite(
        request: UseInviteRequest
    ): LivingLinkResult<UseInviteResponse, NetworkError>

    suspend fun removeUserFromGroup(
        groupId: String,
        userId: String
    ): LivingLinkResult<RemoveUserFromGroupResponse, NetworkError>

    suspend fun makeUserAdmin(
        groupId: String,
        userId: String
    ): LivingLinkResult<MakeUserAdminResponse, NetworkError>
}

class GroupsDefaultRepository(
    private val groupsNetworkDataSource: GroupsNetworkDataSource,
    private val groupStore: GroupStore,
    private val eventBus: EventBus,
    private val scope: CoroutineScope,
    private val fetchAndStoreDataDefaultHandler: FetchAndStoreDataDefaultHandler<Group, NetworkError>,
) : GroupsRepository {

    override val groups = fetchAndStoreDataDefaultHandler(
        events = eventBus.events.mapNotNull { event ->
            when (event) {
                is EventBus.Event.ClearAll -> FetchAndStoreDataEvent.CLEAR
                is EventBus.Event.UpdateGroups -> FetchAndStoreDataEvent.RELOAD
                else -> null
            }
        },
        networkRequest = {
            groupsNetworkDataSource.getGroupsForUser().map { respond ->
                respond.groups.toList().sortedBy { it.createdAt }
            }
        },
        saveToDb = { groupStore.update(it) },
        loadFromDb = { groupStore.groups }
    )

    private val groupsIndexedById: StateFlow<RepositoryState<Map<String, Group>, NetworkError>> =
        groups.map { state ->
            state.mapState { it.associateBy { it.id } }
        }.stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = RepositoryState.Empty(null)
        )

    override fun group(groupId: String): Flow<RepositoryState<Group, NetworkError>> {
        return groupsIndexedById.mapState { it[groupId] }
    }

    override fun resolveUserName(groupId: String, userId: String): Flow<String?> {
        return groupsIndexedById.map {
            it.dataOrNull()?.get(groupId)?.groupMemberIdsToName?.get(userId)
        }
    }

    override suspend fun createGroup(
        request: CreateGroupRequest
    ): LivingLinkResult<CreateGroupResponse, NetworkError> {
        return groupsNetworkDataSource
            .createGroup(request)
            .alsoIfIsSuccess { eventBus.emit(EventBus.Event.UpdateGroups) }
    }

    override suspend fun deleteGroup(
        groupId: String
    ): LivingLinkResult<DeleteGroupResponse, NetworkError> {
        return groupsNetworkDataSource
            .deleteGroup(groupId)
            .alsoIfIsSuccess { eventBus.emit(EventBus.Event.UpdateGroups) }
    }

    override suspend fun leaveGroup(
        groupId: String
    ): LivingLinkResult<LeaveGroupResponse, NetworkError> {
        return groupsNetworkDataSource
            .leaveGroup(LeaveGroupRequest(groupId))
            .alsoIfIsSuccess { eventBus.emit(EventBus.Event.UpdateGroups) }
    }

    override suspend fun createInvite(
        request: CreateInviteRequest
    ): LivingLinkResult<CreateInviteResponse, NetworkError> {
        return groupsNetworkDataSource
            .createInvite(request)
    }

    override suspend fun useInvite(
        request: UseInviteRequest
    ): LivingLinkResult<UseInviteResponse, NetworkError> {
        return groupsNetworkDataSource
            .useInvite(request)
            .alsoIfIsSuccess { eventBus.emit(EventBus.Event.UpdateGroups) }
    }

    override suspend fun removeUserFromGroup(
        groupId: String,
        userId: String
    ): LivingLinkResult<RemoveUserFromGroupResponse, NetworkError> {
        return groupsNetworkDataSource
            .removeUserFromGroup(RemoveUserFromGroupRequest(groupId, userId))
            .alsoIfIsSuccess { eventBus.emit(EventBus.Event.UpdateGroups) }
    }

    override suspend fun makeUserAdmin(
        groupId: String,
        userId: String
    ): LivingLinkResult<MakeUserAdminResponse, NetworkError> {
        return groupsNetworkDataSource
            .makeUserAdmin(MakeUserAdminRequest(groupId, userId))
            .alsoIfIsSuccess { eventBus.emit(EventBus.Event.UpdateGroups) }
    }
}