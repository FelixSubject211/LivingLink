package felix.projekt.livinglink.server.groups.di

import felix.projekt.livinglink.server.groups.application.CheckGroupMembershipDefaultService
import felix.projekt.livinglink.server.groups.application.CreateGroupDefaultUseCase
import felix.projekt.livinglink.server.groups.application.CreateInviteCodeDefaultUseCase
import felix.projekt.livinglink.server.groups.application.DeleteInviteCodeDefaultUseCase
import felix.projekt.livinglink.server.groups.application.GetUserGroupsDefaultUseCase
import felix.projekt.livinglink.server.groups.application.JoinGroupWithInviteCodeDefaultUseCase
import felix.projekt.livinglink.server.groups.application.RemoveUserFromGroupsDefaultService
import felix.projekt.livinglink.server.groups.config.GroupsConfig
import felix.projekt.livinglink.server.groups.config.groupsDefaultConfig
import felix.projekt.livinglink.server.groups.domain.GroupRepository
import felix.projekt.livinglink.server.groups.domain.GroupVersionCache
import felix.projekt.livinglink.server.groups.infrastructure.GroupMongoDbRepository
import felix.projekt.livinglink.server.groups.infrastructure.GroupVersionRedisCache
import felix.projekt.livinglink.server.groups.interfaces.CheckGroupMembershipService
import felix.projekt.livinglink.server.groups.interfaces.CreateGroupUseCase
import felix.projekt.livinglink.server.groups.interfaces.CreateInviteCodeUseCase
import felix.projekt.livinglink.server.groups.interfaces.DeleteInviteCodeUseCase
import felix.projekt.livinglink.server.groups.interfaces.GetUserGroupsUseCase
import felix.projekt.livinglink.server.groups.interfaces.JoinGroupWithInviteCodeUseCase
import felix.projekt.livinglink.server.groups.interfaces.RemoveUserFromGroupsService
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val groupModule = module {
    single<GroupsConfig> { groupsDefaultConfig() }

    single<GroupRepository> {
        GroupMongoDbRepository(
            groupsConfig = get(),
            uuidProvider = get()
        )
    }

    single<GroupVersionCache> {
        GroupVersionRedisCache(
            groupsConfig = get()
        )
    }

    factoryOf(::RemoveUserFromGroupsDefaultService) bind RemoveUserFromGroupsService::class
    factoryOf(::CheckGroupMembershipDefaultService) bind CheckGroupMembershipService::class
    factoryOf(::GetUserGroupsDefaultUseCase) bind GetUserGroupsUseCase::class
    factoryOf(::CreateGroupDefaultUseCase) bind CreateGroupUseCase::class
    factoryOf(::CreateInviteCodeDefaultUseCase) bind CreateInviteCodeUseCase::class
    factoryOf(::DeleteInviteCodeDefaultUseCase) bind DeleteInviteCodeUseCase::class
    factoryOf(::JoinGroupWithInviteCodeDefaultUseCase) bind JoinGroupWithInviteCodeUseCase::class
}
