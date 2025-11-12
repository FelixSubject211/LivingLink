package felix.projekt.livinglink.server.groups.application

import felix.projekt.livinglink.server.groups.domain.GroupRepository
import felix.projekt.livinglink.server.groups.domain.GroupVersionCache
import felix.projekt.livinglink.server.groups.domain.JoinGroupResponse
import felix.projekt.livinglink.server.groups.interfaces.JoinGroupWithInviteCodeUseCase

class JoinGroupWithInviteCodeDefaultUseCase(
    private val groupRepository: GroupRepository,
    private val groupVersionCache: GroupVersionCache
) : JoinGroupWithInviteCodeUseCase {
    override suspend fun invoke(
        userId: String,
        username: String,
        inviteCodeKey: String
    ): JoinGroupResponse {
        val group = groupRepository.getGroupByInviteCodeKey(inviteCodeKey)
            ?: return JoinGroupResponse.InviteCodeNotFound

        val result = groupRepository.updateWithOptimisticLocking(group.id) { current ->
            val inviteCodeId = groupRepository.getInviteCodeIdByKey(inviteCodeKey)
                ?: return@updateWithOptimisticLocking GroupRepository.UpdateOperationResult.NoUpdate(
                    JoinGroupResponse.InviteCodeNotFound
                )

            if (current.memberIdToMember.containsKey(userId)) {
                return@updateWithOptimisticLocking GroupRepository.UpdateOperationResult.NoUpdate(
                    JoinGroupResponse.AlreadyMember
                )
            }

            val updated = current
                .addMember(userId = userId, username = username)
                .incrementInviteCodeUsage(inviteCodeId)

            GroupRepository.UpdateOperationResult.Updated(
                newEntity = updated,
                response = JoinGroupResponse.Success(updated)
            )
        }

        val updatedGroup = result.entity
        updatedGroup?.memberIdToMember?.values?.forEach { member ->
            groupVersionCache.addOrUpdateGroupVersionIfUserExists(
                userId = member.id,
                groupId = updatedGroup.id,
                version = updatedGroup.version
            )
        }

        return result.response
    }
}