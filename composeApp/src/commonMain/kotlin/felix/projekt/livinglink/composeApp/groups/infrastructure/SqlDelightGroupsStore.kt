package felix.projekt.livinglink.composeApp.groups.infrastructure

import felix.projekt.livinglink.composeApp.core.Database
import felix.projekt.livinglink.composeApp.core.Groups
import felix.projekt.livinglink.composeApp.groups.domain.Group
import felix.projekt.livinglink.composeApp.groups.domain.GroupsStore
import felix.projekt.livinglink.shared.json
import kotlinx.serialization.Serializable

class SqlDelightGroupsStore(
    private val database: Database
) : GroupsStore {
    private val queries = database.groupsDatabaseQueries

    override fun getGroups(): Map<String, Group> {
        val storedGroups = queries.selectGroups().executeAsList()
        return storedGroups.associate { storedGroup ->
            storedGroup.id to storedGroup.toDomain()
        }
    }

    override fun saveGroups(groups: Map<String, Group>) {
        queries.transaction {
            queries.deleteAllGroups()
            groups.values.forEach { group ->
                val groupJson = group.toJson()
                queries.upsertGroup(
                    id = group.id,
                    groupJson = groupJson
                )
            }
        }
    }

    override fun clear() {
        queries.deleteAllGroups()
    }

    private fun Group.toJson(): String {
        val storedGroup = StoredGroup.fromDomain(group = this)
        return json.encodeToString(storedGroup)
    }

    private fun StoredGroup.toDomain(): Group {
        return Group(
            id = id,
            name = name,
            memberIdToMember = memberIdToMember.mapValues { entry ->
                Group.Member(
                    id = entry.value.id,
                    username = entry.value.username
                )
            },
            inviteCodes = inviteCodes.map { inviteCode ->
                Group.InviteCode(
                    id = inviteCode.id,
                    name = inviteCode.name,
                    creatorId = inviteCode.creatorId,
                    usages = inviteCode.usages
                )
            },
            version = version
        )
    }

    private fun Groups.toDomain(): Group {
        val storedGroup = json.decodeFromString<StoredGroup>(groupJson)
        return storedGroup.toDomain()
    }

    @Serializable
    private data class StoredGroup(
        val id: String,
        val name: String,
        val memberIdToMember: Map<String, StoredMember>,
        val inviteCodes: List<StoredInviteCode>,
        val version: Long
    ) {
        companion object {
            fun fromDomain(group: Group): StoredGroup {
                return StoredGroup(
                    id = group.id,
                    name = group.name,
                    memberIdToMember = group.memberIdToMember.mapValues { entry ->
                        StoredMember(
                            id = entry.value.id,
                            username = entry.value.username
                        )
                    },
                    inviteCodes = group.inviteCodes.map { inviteCode ->
                        StoredInviteCode(
                            id = inviteCode.id,
                            name = inviteCode.name,
                            creatorId = inviteCode.creatorId,
                            usages = inviteCode.usages
                        )
                    },
                    version = group.version
                )
            }
        }

        @Serializable
        data class StoredMember(
            val id: String,
            val username: String
        )

        @Serializable
        data class StoredInviteCode(
            val id: String,
            val name: String,
            val creatorId: String,
            val usages: Int
        )
    }
}
