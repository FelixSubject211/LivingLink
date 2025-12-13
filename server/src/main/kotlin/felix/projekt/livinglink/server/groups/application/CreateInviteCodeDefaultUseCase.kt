package felix.projekt.livinglink.server.groups.application

import felix.projekt.livinglink.server.groups.domain.CreateInviteCodeResponse
import felix.projekt.livinglink.server.groups.domain.Group
import felix.projekt.livinglink.server.groups.domain.GroupRepository
import felix.projekt.livinglink.server.groups.domain.GroupVersionCache
import felix.projekt.livinglink.server.groups.interfaces.CreateInviteCodeUseCase
import java.security.SecureRandom
import java.util.Base64

class CreateInviteCodeDefaultUseCase(
    private val groupRepository: GroupRepository,
    private val groupVersionCache: GroupVersionCache,
    private val uuidProvider: () -> String
) : CreateInviteCodeUseCase {
    override suspend fun invoke(
        userId: String,
        groupId: String,
        inviteCodeName: String
    ): CreateInviteCodeResponse {
        val inviteCode = Group.InviteCode(
            id = uuidProvider(),
            key = generateInviteCode(),
            name = inviteCodeName,
            creatorId = userId,
            usages = 0
        )

        val result = groupRepository.updateWithOptimisticLocking(groupId) { group ->
            require(group.memberIdToMember.containsKey(userId)) {
                "User $userId is not a member of group $groupId"
            }

            GroupRepository.UpdateOperationResult.Updated(
                newEntity = group.addInviteCode(inviteCode),
                response = CreateInviteCodeResponse.Success(inviteCode.key)
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

    private fun generateInviteCode(bytes: Int = 14): String {
        val random = SecureRandom()
        val buffer = ByteArray(bytes)
        random.nextBytes(buffer)
        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(buffer)
            .uppercase()
    }
}