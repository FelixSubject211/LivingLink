package felix.projekt.livinglink.server.groups.interfaces

import felix.projekt.livinglink.server.groups.domain.Group

interface CreateGroupUseCase {
    suspend operator fun invoke(userId: String, username: String, groupName: String): Group
}