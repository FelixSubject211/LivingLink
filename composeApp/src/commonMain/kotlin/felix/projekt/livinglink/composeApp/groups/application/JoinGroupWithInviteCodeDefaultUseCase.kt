package felix.projekt.livinglink.composeApp.groups.application

import felix.projekt.livinglink.composeApp.core.domain.Result
import felix.projekt.livinglink.composeApp.groups.domain.Group
import felix.projekt.livinglink.composeApp.groups.domain.GroupsRepository
import felix.projekt.livinglink.composeApp.groups.domain.JoinGroupResponse
import felix.projekt.livinglink.composeApp.groups.interfaces.JoinGroupWithInviteCodeUseCase

class JoinGroupWithInviteCodeDefaultUseCase(
    private val groupsRepository: GroupsRepository
) : JoinGroupWithInviteCodeUseCase {
    override suspend fun invoke(inviteCodeKey: String): JoinGroupWithInviteCodeUseCase.Response {
        if (inviteCodeKey.isEmpty()) {
            return JoinGroupWithInviteCodeUseCase.Response.InvalidInviteCode
        }

        return when (val result = groupsRepository.joinGroup(inviteCodeKey)) {
            is Result.Success -> {
                when (val data = result.data) {
                    is JoinGroupResponse.Success -> {
                        JoinGroupWithInviteCodeUseCase.Response.Success(data.group.toResponseGroup())
                    }

                    is JoinGroupResponse.InviteCodeNotFound -> JoinGroupWithInviteCodeUseCase.Response.InvalidInviteCode

                    is JoinGroupResponse.AlreadyMember -> JoinGroupWithInviteCodeUseCase.Response.AlreadyMember
                }
            }

            is Result.Error -> JoinGroupWithInviteCodeUseCase.Response.NetworkError
        }
    }

    private fun Group.toResponseGroup() = JoinGroupWithInviteCodeUseCase.Group(
        id = id,
        name = name,
        memberCount = memberIdToMember.size
    )
}
