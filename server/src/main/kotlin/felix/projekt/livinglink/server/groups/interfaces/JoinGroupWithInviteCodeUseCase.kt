package felix.projekt.livinglink.server.groups.interfaces

import felix.projekt.livinglink.server.groups.domain.JoinGroupResponse

interface JoinGroupWithInviteCodeUseCase {
    suspend operator fun invoke(
        userId: String,
        username: String,
        inviteCodeKey: String
    ): JoinGroupResponse
}
