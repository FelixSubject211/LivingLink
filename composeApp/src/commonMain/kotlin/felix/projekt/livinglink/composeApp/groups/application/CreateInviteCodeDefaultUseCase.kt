package felix.projekt.livinglink.composeApp.groups.application

import felix.projekt.livinglink.composeApp.core.domain.Result
import felix.projekt.livinglink.composeApp.groups.domain.CreateInviteCodeResponse
import felix.projekt.livinglink.composeApp.groups.domain.GroupsRepository
import felix.projekt.livinglink.composeApp.groups.interfaces.CreateInviteCodeUseCase

class CreateInviteCodeDefaultUseCase(
    private val groupsRepository: GroupsRepository
) : CreateInviteCodeUseCase {
    override suspend fun invoke(groupId: String, inviteCodeName: String): CreateInviteCodeUseCase.Response {
        val response = groupsRepository.createInviteCode(
            groupId = groupId,
            inviteCodeName = inviteCodeName
        )
        return when (response) {
            is Result.Success -> {
                when (response.data) {
                    is CreateInviteCodeResponse.Success -> {
                        CreateInviteCodeUseCase.Response.Success(key = response.data.key)
                    }
                }
            }

            is Result.Error<*> -> {
                CreateInviteCodeUseCase.Response.NetworkError
            }
        }
    }
}