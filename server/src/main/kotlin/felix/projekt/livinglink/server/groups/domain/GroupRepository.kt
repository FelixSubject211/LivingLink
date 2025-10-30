package felix.projekt.livinglink.server.groups.domain


interface GroupRepository {
    fun getGroupsForMember(userId: String): Map<String, Group>
    fun getGroupById(groupId: String): Group?
    fun createGroup(groupName: String): Group
    fun updateWithOptimisticLocking(
        groupId: String,
        maxRetries: Int = 3,
        update: (Group) -> Group
    ): Group

    fun deleteGroup(groupId: String)
    fun close()
}