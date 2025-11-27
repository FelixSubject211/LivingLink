package felix.projekt.livinglink.composeApp.groups.interfaces

import kotlinx.coroutines.flow.Flow

interface GetGroupMemberIdToMemberNameService {
    operator fun invoke(groupId: String): Flow<Map<String, String>?>
}