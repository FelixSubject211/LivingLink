package felix.projekt.livinglink.server.groups.domain


interface GroupRepository {
    fun getGroupsForMember(userId: String): Map<String, Group>
    fun getGroupById(groupId: String): Group?
    fun getGroupByInviteCodeKey(inviteCodeKey: String): Group?
    fun getInviteCodeIdByKey(inviteCodeKey: String): String?
    fun createGroup(groupName: String): Group
    fun <R> updateWithOptimisticLocking(
        groupId: String,
        maxRetries: Int = 3,
        update: (Group) -> UpdateOperationResult<Group, R>
    ): UpdateResult<Group, R>

    fun deleteGroup(groupId: String)
    fun close()

    sealed class UpdateOperationResult<out T, out R> {
        data class Updated<T, R>(val newEntity: T, val response: R) : UpdateOperationResult<T, R>()
        data class NoUpdate<R>(val response: R) : UpdateOperationResult<Nothing, R>()
    }

    data class UpdateResult<T, R>(
        val entity: T?,
        val response: R
    )
}