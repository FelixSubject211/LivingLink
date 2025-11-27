package felix.projekt.livinglink.composeApp.groups.application

import felix.projekt.livinglink.composeApp.groups.domain.GroupsRepository
import felix.projekt.livinglink.composeApp.groups.interfaces.GetGroupMemberIdToMemberNameService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetGroupMemberIdToMemberNameDefaultService(
    private val groupsRepository: GroupsRepository
) : GetGroupMemberIdToMemberNameService {
    override fun invoke(groupId: String): Flow<Map<String, String>?> {
        return groupsRepository.getGroups.map { repositoryState ->
            when (repositoryState) {
                GroupsRepository.GroupsRepositoryState.Loading -> {
                    null
                }

                is GroupsRepository.GroupsRepositoryState.Data -> {
                    val group = repositoryState.groupIdToGroup[groupId]
                    group?.memberIdToMember?.mapValues { it.value.username }
                }
            }
        }
    }
}