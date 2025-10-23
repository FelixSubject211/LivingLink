package felix.projekt.livinglink.composeApp.groups.application

import felix.projekt.livinglink.composeApp.core.domain.Result
import felix.projekt.livinglink.composeApp.groups.domain.GroupsRepository
import felix.projekt.livinglink.composeApp.groups.interfaces.CreateGroupUseCase

class CreateGroupDefaultUseCase(
    private val groupsRepository: GroupsRepository,
) : CreateGroupUseCase {
    override suspend fun invoke(groupName: String): CreateGroupUseCase.Response {
        val response = groupsRepository.createGroup(groupName = groupName)
        return when (response) {
            is Result.Success -> {
                CreateGroupUseCase.Response.Success
            }

            is Result.Error -> {
                CreateGroupUseCase.Response.NetworkError
            }
        }
    }
}