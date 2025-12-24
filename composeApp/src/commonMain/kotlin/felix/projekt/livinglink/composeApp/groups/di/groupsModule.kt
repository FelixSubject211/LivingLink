package felix.projekt.livinglink.composeApp.groups.di

import felix.projekt.livinglink.composeApp.auth.domain.AuthTokenManager
import felix.projekt.livinglink.composeApp.groups.application.CreateGroupDefaultUseCase
import felix.projekt.livinglink.composeApp.groups.application.CreateInviteCodeDefaultUseCase
import felix.projekt.livinglink.composeApp.groups.application.DeleteInviteCodeDefaultUseCase
import felix.projekt.livinglink.composeApp.groups.application.GetGroupDefaultUseCase
import felix.projekt.livinglink.composeApp.groups.application.GetGroupMemberIdToMemberNameDefaultService
import felix.projekt.livinglink.composeApp.groups.application.GetGroupsDefaultUseCase
import felix.projekt.livinglink.composeApp.groups.application.GroupsDefaultRepository
import felix.projekt.livinglink.composeApp.groups.application.JoinGroupWithInviteCodeDefaultUseCase
import felix.projekt.livinglink.composeApp.groups.domain.GroupsRepository
import felix.projekt.livinglink.composeApp.groups.infrastructure.GroupsNetworkDefaultDataSource
import felix.projekt.livinglink.composeApp.groups.interfaces.CreateGroupUseCase
import felix.projekt.livinglink.composeApp.groups.interfaces.CreateInviteCodeUseCase
import felix.projekt.livinglink.composeApp.groups.interfaces.DeleteInviteCodeUseCase
import felix.projekt.livinglink.composeApp.groups.interfaces.GetGroupMemberIdToMemberNameService
import felix.projekt.livinglink.composeApp.groups.interfaces.GetGroupUseCase
import felix.projekt.livinglink.composeApp.groups.interfaces.GetGroupsUseCase
import felix.projekt.livinglink.composeApp.groups.interfaces.JoinGroupWithInviteCodeUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val groupsModule = module {
    single<GroupsRepository>(createdAtStart = true) {
        GroupsDefaultRepository(
            groupsNetworkDataSource = GroupsNetworkDefaultDataSource(
                httpClient = get<AuthTokenManager>().client
            ),
            getAuthStateService = get(),
            scope = get()
        )
    }

    factoryOf(::GetGroupMemberIdToMemberNameDefaultService) bind GetGroupMemberIdToMemberNameService::class
    factoryOf(::GetGroupsDefaultUseCase) bind GetGroupsUseCase::class
    factoryOf(::GetGroupDefaultUseCase) bind GetGroupUseCase::class
    factoryOf(::CreateGroupDefaultUseCase) bind CreateGroupUseCase::class
    factoryOf(::CreateInviteCodeDefaultUseCase) bind CreateInviteCodeUseCase::class
    factoryOf(::DeleteInviteCodeDefaultUseCase) bind DeleteInviteCodeUseCase::class
    factoryOf(::JoinGroupWithInviteCodeDefaultUseCase) bind JoinGroupWithInviteCodeUseCase::class
}