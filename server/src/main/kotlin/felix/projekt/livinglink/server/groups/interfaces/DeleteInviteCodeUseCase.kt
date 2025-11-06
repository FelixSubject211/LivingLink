package felix.projekt.livinglink.server.groups.interfaces

import felix.projekt.livinglink.server.groups.domain.DeleteInviteCodeResponse

interface DeleteInviteCodeUseCase {
    suspend operator fun invoke(
        userId: String,
        groupId: String,
        inviteCodeId: String
    ): DeleteInviteCodeResponse
}