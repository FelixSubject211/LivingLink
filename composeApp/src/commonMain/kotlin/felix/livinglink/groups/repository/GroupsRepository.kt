package felix.livinglink.groups.repository

import felix.livinglink.common.model.LivingLinkResult
import felix.livinglink.common.model.RepositoryState
import felix.livinglink.common.model.map
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
import felix.livinglink.group.UseInviteRequest
import felix.livinglink.group.UseInviteResponse
import felix.livinglink.groups.network.GroupsNetworkDataSource
import felix.livinglink.groups.store.GroupStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface GroupsRepository {
    val groups: Flow<RepositoryState<List<Group>, NetworkError>>

    suspend fun createGroup(
        request: CreateGroupRequest
    ): LivingLinkResult<CreateGroupResponse, NetworkError>

    suspend fun deleteGroup(
        groupId: String
    ): LivingLinkResult<DeleteGroupResponse, NetworkError>

    suspend fun createInvite(
        request: CreateInviteRequest
    ): LivingLinkResult<CreateInviteResponse, NetworkError>

    suspend fun useInvite(
        request: UseInviteRequest
    ): LivingLinkResult<UseInviteResponse, NetworkError>
}

class GroupsDefaultRepository(
    private val groupsNetworkDataSource: GroupsNetworkDataSource,
    private val groupStore: GroupStore,
    private val eventBus: EventBus,
    private val fetchAndStoreDataDefaultHandler: FetchAndStoreDataDefaultHandler<Group, NetworkError>
) : GroupsRepository {

    override val groups = fetchAndStoreDataDefaultHandler(
        events = eventBus.events.map { event ->
            when (event) {
                EventBus.Event.ClearAll -> FetchAndStoreDataEvent.CLEAR
                EventBus.Event.UpdateGroups -> FetchAndStoreDataEvent.RELOAD
            }
        },
        networkRequest = {
            groupsNetworkDataSource.getGroupsForUser().map { respond ->
                respond.groups.toList().sortedBy { it.name }
            }
        },
        saveToDb = { groupStore.update(it) },
        loadFromDb = { groupStore.groups }
    )

    override suspend fun createGroup(
        request: CreateGroupRequest
    )
            : LivingLinkResult<CreateGroupResponse, NetworkError> {
        return groupsNetworkDataSource
            .createGroup(request)
            .also { eventBus.emit(EventBus.Event.UpdateGroups) }
    }

    override suspend fun deleteGroup(
        groupId: String
    ): LivingLinkResult<DeleteGroupResponse, NetworkError> {
        return groupsNetworkDataSource
            .deleteGroup(groupId)
            .also { eventBus.emit(EventBus.Event.UpdateGroups) }
    }

    override suspend fun createInvite(
        request: CreateInviteRequest
    ): LivingLinkResult<CreateInviteResponse, NetworkError> {
        return groupsNetworkDataSource
            .createInvite(request)
            .also { eventBus.emit(EventBus.Event.UpdateGroups) }
    }

    override suspend fun useInvite(
        request: UseInviteRequest
    ): LivingLinkResult<UseInviteResponse, NetworkError> {
        return groupsNetworkDataSource.useInvite(request)
    }
}