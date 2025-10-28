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
                GroupsRepository.GroupsRepositoryState.Loading -> {
                    GetGroupsUseCase.Response.Loading
                }

                is GroupsRepository.GroupsRepositoryState.Data -> {
                    GetGroupsUseCase.Response.Data(repositoryState.groups.toResponse())
                }
            }
        }
    }

    private fun Map<String, Group>.toResponse() = this.mapValues { group ->
        GetGroupsUseCase.Group(
            id = group.value.id,
            name = group.value.name,
            memberCount = group.value.memberIdToMember.size
        )
    }.values.toList()
}