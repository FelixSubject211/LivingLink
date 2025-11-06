package felix.projekt.livinglink.composeApp.groups.application

import felix.projekt.livinglink.composeApp.core.domain.Result
import felix.projekt.livinglink.composeApp.groups.domain.DeleteInviteCodeResponse
import felix.projekt.livinglink.composeApp.groups.domain.GroupsRepository
import felix.projekt.livinglink.composeApp.groups.interfaces.DeleteInviteCodeUseCase

class DeleteInviteCodeDefaultUseCase(
    private val groupsRepository: GroupsRepository
) : DeleteInviteCodeUseCase {
    override suspend fun invoke(
        groupId: String,
        inviteCodeId: String
    ): DeleteInviteCodeUseCase.Response {
        val response = groupsRepository.deleteInviteCode(
            groupId = groupId,
            inviteCodeId = inviteCodeId
        )
        return when (response) {
            is Result.Success -> {
                when (response.data) {
                    is DeleteInviteCodeResponse.Success -> {
                        DeleteInviteCodeUseCase.Response.Success
                    }
                }
            }

            is Result.Error<*> -> {
                DeleteInviteCodeUseCase.Response.NetworkError
            }
        }
    }
}