package felix.livinglink.groups

import felix.livinglink.common.GroupInvitesTable
import felix.livinglink.common.GroupMembersTable
import felix.livinglink.common.GroupsTable
import felix.livinglink.common.TimeService
import felix.livinglink.common.UsersTable
import felix.livinglink.common.UuidFactory
import felix.livinglink.group.Group
import kotlinx.datetime.toKotlinInstant
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.associate
import org.ktorm.dsl.delete
import org.ktorm.dsl.eq
import org.ktorm.dsl.forEach
import org.ktorm.dsl.from
import org.ktorm.dsl.inList
import org.ktorm.dsl.innerJoin
import org.ktorm.dsl.insert
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.where
import java.time.Instant
import java.util.UUID

interface GroupStore {
    fun getGroupsForUser(userId: String): Set<Group>
    fun createGroup(name: String, creatorUserId: String): String?
    fun deleteGroup(groupId: String): Boolean
    fun createInviteCode(groupId: String, createdBy: String): String?
    fun useInviteCode(code: String, userId: String): String?
    fun getUserIdsInGroup(groupId: String): List<String>
    fun isUserIdInGroup(userId: String, groupId: String): Boolean
    fun getGroupIdsForUser(userId: String): List<String>
}

class GroupDefaultStore(
    private val timeService: TimeService,
    private val uuidFactory: UuidFactory,
    private val database: Database
): GroupStore {

    override fun getGroupsForUser(userId: String): Set<Group> {
        return database.useTransaction {
            val groupIds = database
                .from(GroupMembersTable)
                .select(GroupMembersTable.groupId)
                .where { GroupMembersTable.userId eq userId }
                .map { it[GroupMembersTable.groupId]!! }

            if (groupIds.isEmpty()) return@useTransaction emptySet()

            val groups = database
                .from(GroupsTable)
                .select()
                .where { GroupsTable.id inList groupIds }
                .associate { row ->
                    val groupId = row[GroupsTable.id]!!
                    groupId to Group(
                        id = groupId,
                        name = row[GroupsTable.name]!!,
                        createdAt = row[GroupsTable.createdAt]!!.toKotlinInstant(),
                        groupMemberIdsToName = mutableMapOf()
                    )
                }

            database
                .from(GroupMembersTable)
                .innerJoin(UsersTable, on = GroupMembersTable.userId eq UsersTable.id)
                .select(
                    GroupMembersTable.groupId,
                    UsersTable.id,
                    UsersTable.username
                )
                .where { GroupMembersTable.groupId inList groupIds }
                .forEach { row ->
                    val groupId = row[GroupMembersTable.groupId]!!
                    val memberId = row[UsersTable.id]!!
                    val username = row[UsersTable.username]!!

                    val group = groups[groupId]
                    if (group != null) {
                        (group.groupMemberIdsToName as MutableMap)[memberId] = username
                    }
                }

            return@useTransaction groups.values.toSet()
        }
    }

    override fun createGroup(name: String, creatorUserId: String): String? {
        val groupId = UUID.randomUUID().toString()
        val now = Instant.ofEpochMilli(timeService.currentTimeMillis())

        return try {
            database.useTransaction {
                val rowsInserted = database.insert(GroupsTable) {
                    set(it.id, groupId)
                    set(it.name, name)
                    set(it.createdAt, now)
                }

                if (rowsInserted == 0) return null

                val memberInsert = database.insert(GroupMembersTable) {
                    set(it.groupId, groupId)
                    set(it.userId, creatorUserId)
                    set(it.createdAt, now)
                }

                if (memberInsert > 0) groupId else null
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun deleteGroup(groupId: String): Boolean {
        return try {
            database.useTransaction {
                database.delete(GroupMembersTable) {
                    it.groupId eq groupId
                }
                val rowsDeleted = database.delete(GroupsTable) {
                    it.id eq groupId
                }
                rowsDeleted > 0
            }
        } catch (e: Exception) {
            false
        }
    }

    override fun createInviteCode(groupId: String, createdBy: String): String? {
        val now = Instant.ofEpochMilli(timeService.currentTimeMillis())
        val code = uuidFactory().take(8)

        return try {
            database.useTransaction {
                val isMember = database
                    .from(GroupMembersTable)
                    .select()
                    .where {
                        (GroupMembersTable.groupId eq groupId) and
                                (GroupMembersTable.userId eq createdBy)
                    }
                    .totalRecordsInAllPages > 0

                if (!isMember) return null

                val inserted = database.insert(GroupInvitesTable) {
                    set(it.code, code)
                    set(it.groupId, groupId)
                    set(it.createdBy, createdBy)
                    set(it.createdAt, now)
                }

                if (inserted > 0) code else null
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun useInviteCode(code: String, userId: String): String? {
        val now = Instant.ofEpochMilli(timeService.currentTimeMillis())

        return try {
            database.useTransaction {
                val groupId = database
                    .from(GroupInvitesTable)
                    .select(GroupInvitesTable.groupId)
                    .where { GroupInvitesTable.code eq code }
                    .map { it[GroupInvitesTable.groupId] }
                    .firstOrNull() ?: return null

                val alreadyMember = database
                    .from(GroupMembersTable)
                    .select()
                    .where {
                        (GroupMembersTable.groupId eq groupId) and
                                (GroupMembersTable.userId eq userId)
                    }
                    .totalRecordsInAllPages > 0

                if (alreadyMember) return null

                database.insert(GroupMembersTable) {
                    set(it.groupId, groupId)
                    set(it.userId, userId)
                    set(it.createdAt, now)
                }

                database.delete(GroupInvitesTable) {
                    it.code eq code
                }

                return groupId
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun getUserIdsInGroup(groupId: String): List<String> {
        return database
            .from(GroupMembersTable)
            .select(GroupMembersTable.userId)
            .where { GroupMembersTable.groupId eq groupId }
            .map { it[GroupMembersTable.userId]!! }
    }

    override fun isUserIdInGroup(userId: String, groupId: String): Boolean {
        return database
            .from(GroupMembersTable)
            .select()
            .where {
                (GroupMembersTable.groupId eq groupId) and
                        (GroupMembersTable.userId eq userId)
            }
            .totalRecordsInAllPages > 0
    }

    override fun getGroupIdsForUser(userId: String): List<String> {
        return database
            .from(GroupMembersTable)
            .select(GroupMembersTable.groupId)
            .where { GroupMembersTable.userId eq userId }
            .map { it[GroupMembersTable.groupId]!! }
    }
}