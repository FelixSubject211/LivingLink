package felix.projekt.livinglink.server.groups.domain

interface GroupVersionCache {
    suspend fun setGroupVersions(userId: String, versions: GroupVersions)
    suspend fun getGroupVersions(userId: String): GroupVersions?
    suspend fun addOrUpdateGroupVersionIfUserExists(userId: String, groupId: String, version: Long)
    suspend fun deleteGroupVersions(userId: String)

    data class GroupVersions(val groupIdsToGroupVersion: Map<String, Long>)
}