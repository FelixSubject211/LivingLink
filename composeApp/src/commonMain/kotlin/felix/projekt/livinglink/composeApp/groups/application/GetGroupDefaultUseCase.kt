package felix.projekt.livinglink.composeApp.groups.application

import felix.projekt.livinglink.composeApp.groups.domain.Group
import felix.projekt.livinglink.composeApp.groups.domain.GroupsRepository
import felix.projekt.livinglink.composeApp.groups.interfaces.GetGroupUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetGroupDefaultUseCase(
    private val groupsRepository: GroupsRepository
) : GetGroupUseCase {
    override fun invoke(groupId: String): Flow<GetGroupUseCase.Response> {
        return groupsRepository.getGroups.map { repositoryState ->
            when (repositoryState) {
                GroupsRepository.GroupsRepositoryState.Loading -> {
                    GetGroupUseCase.Response.Loading
                }

                is GroupsRepository.GroupsRepositoryState.Data -> {
                    val group = repositoryState.groupIdToGroup[groupId]
                    if (group == null) {
                        GetGroupUseCase.Response.Loading
                    } else {
                        GetGroupUseCase.Response.Data(group.toResponse())
                    }
                }
            }
        }
    }

    private fun Group.toResponse() = GetGroupUseCase.Group(
        id = this.id,
        name = this.name
    )
}