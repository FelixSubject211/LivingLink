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
        val result = groupRepository.updateWithOptimisticLocking(
            groupId = groupId
        ) { group ->
            require(group.memberIdToMember.containsKey(userId)) {
                "User $userId is not a member of group $groupId"
            }

            val updated = group.removeInviteCode(inviteCodeId)
            GroupRepository.UpdateOperationResult.Updated(
                newEntity = updated,
                response = DeleteInviteCodeResponse.Success
            )
        }

        result.entity?.memberIdToMember?.values?.forEach { member ->
            groupVersionCache.addOrUpdateGroupVersionIfUserExists(
                userId = member.id,
                groupId = groupId,
                version = result.entity.version
            )
        }

        return result.response
    }

}