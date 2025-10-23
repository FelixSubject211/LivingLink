package felix.projekt.livinglink.composeApp.groups.application

import felix.projekt.livinglink.composeApp.groups.domain.Group
import felix.projekt.livinglink.composeApp.groups.domain.GroupsRepository
import felix.projekt.livinglink.composeApp.groups.interfaces.GetGroupsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetGroupsDefaultUseCase(
    private val groupsRepository: GroupsRepository
) : GetGroupsUseCase {
    override fun invoke(): Flow<GetGroupsUseCase.Response> {
        return groupsRepository.getGroups.map { repositoryState ->
            when (repositoryState) {
                GroupsRepository.GroupRepositoryState.Loading -> {
                    GetGroupsUseCase.Response.Loading
                }

                is GroupsRepository.GroupRepositoryState.Data -> {
                    GetGroupsUseCase.Response.Data(repositoryState.groups.toResponse())
                }
            }
        }
    }

    private fun List<Group>.toResponse() = this.map { group ->
        GetGroupsUseCase.Group(
            id = group.id,
            name = group.name,
            memberCount = group.memberIdToMember.size
        )
    }
}