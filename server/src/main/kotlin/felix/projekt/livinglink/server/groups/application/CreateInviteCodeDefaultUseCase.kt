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

        val updatedGroup = groupRepository.updateWithOptimisticLocking(groupId = groupId) { group ->
            group.addInviteCode(inviteCode)
        } ?: throw IllegalStateException()

        updatedGroup.memberIdToMember.values.forEach { member ->
            groupVersionCache.addOrUpdateGroupVersionIfUserExists(
                userId = member.id,
                groupId = groupId,
                version = updatedGroup.version
            )
        }
        return CreateInviteCodeResponse.Success(key = inviteCode.key)
    }

    fun generateInviteCode(bytes: Int = 14): String {
        val random = SecureRandom()
        val buffer = ByteArray(bytes)
        random.nextBytes(buffer)
        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(buffer)
            .uppercase()
    }
}