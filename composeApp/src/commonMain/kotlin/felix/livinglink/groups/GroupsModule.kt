package felix.livinglink.groups

import felix.livinglink.auth.AuthModule
import felix.livinglink.common.CommonModule
import felix.livinglink.common.network.NetworkError
import felix.livinglink.common.repository.FetchAndStoreDataDefaultHandler
import felix.livinglink.event.EventModule
import felix.livinglink.group.Group
import felix.livinglink.groups.network.GroupNetworkDefaultDataSource
import felix.livinglink.groups.repository.GroupsDefaultRepository
import felix.livinglink.groups.repository.GroupsRepository
import felix.livinglink.groups.store.GroupDefaultStore

interface GroupsModule {
    val groupsRepository: GroupsRepository
}

fun defaultGroupsModule(
    commonModule: CommonModule,
    authModule: AuthModule,
    eventModule: EventModule
): GroupsModule {
    val groupsNetworkDataSource = GroupNetworkDefaultDataSource(
        authenticatedHttpClient = authModule.authenticatedHttpClient.client
    )

    val groupStore = GroupDefaultStore()

    val fetchAndStoreDataDefaultHandler = FetchAndStoreDataDefaultHandler<Group, NetworkError>(
        scope = commonModule.defaultScope
    )

    return object : GroupsModule {
        override val groupsRepository = GroupsDefaultRepository(
            groupsNetworkDataSource = groupsNetworkDataSource,
            groupStore = groupStore,
            eventBus = eventModule.eventBus,
            scope = commonModule.defaultScope,
            fetchAndStoreDataDefaultHandler = fetchAndStoreDataDefaultHandler
        )
    }
}