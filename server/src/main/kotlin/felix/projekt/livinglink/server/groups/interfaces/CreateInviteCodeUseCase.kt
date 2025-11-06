package felix.projekt.livinglink.server.groups.interfaces

import felix.projekt.livinglink.server.groups.domain.CreateInviteCodeResponse

interface CreateInviteCodeUseCase {
    suspend operator fun invoke(
        userId: String,
        groupId: String,
        inviteCodeName: String
    ): CreateInviteCodeResponse
}