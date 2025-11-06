package felix.projekt.livinglink.server.groups.application

import felix.projekt.livinglink.server.groups.domain.DeleteInviteCodeResponse
import felix.projekt.livinglink.server.groups.domain.GroupRepository
import felix.projekt.livinglink.server.groups.domain.GroupVersionCache
import felix.projekt.livinglink.server.groups.interfaces.DeleteInviteCodeUseCase

class DeleteInviteCodeDefaultUseCase(
    private val groupRepository: GroupRepository,
    private val groupVersionCache: GroupVersionCache
) : DeleteInviteCodeUseCase {
    override suspend fun invoke(
        userId: String,
        groupId: String,
        inviteCodeId: String
    ): DeleteInviteCodeResponse {
        val updatedGroup = groupRepository.updateWithOptimisticLocking(groupId = groupId) { group ->
            if (!group.memberIdToMember.contains(userId)) {
                return@updateWithOptimisticLocking null
            }
            group.removeInviteCode(inviteCodeId = inviteCodeId)
        } ?: throw IllegalStateException()

        updatedGroup.memberIdToMember.values.forEach { member ->
            groupVersionCache.addOrUpdateGroupVersionIfUserExists(
                userId = member.id,
                groupId = groupId,
                version = updatedGroup.version
            )
        }

        return DeleteInviteCodeResponse.Success
    }
}