package felix.projekt.livinglink.server.groups.infrastructure

import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import felix.projekt.livinglink.server.core.domain.OptimisticLockingException
import felix.projekt.livinglink.server.groups.config.GroupsConfig
import felix.projekt.livinglink.server.groups.domain.Group
import felix.projekt.livinglink.server.groups.domain.GroupRepository
import org.bson.Document

class GroupMongoDbRepository(
    private val groupsConfig: GroupsConfig,
    private val uuidProvider: () -> String
) : GroupRepository {

    private val mongoClient = MongoClients.create(
        "mongodb://${groupsConfig.mongoUsername}:${groupsConfig.mongoPassword}@${groupsConfig.mongoHost}:${groupsConfig.mongoPort}/?maxPoolSize=10&w=majority"
    )
    private val database: MongoDatabase = mongoClient.getDatabase(groupsConfig.mongoDatabase)
    private val collection: MongoCollection<Document> = database.getCollection("groups").also {
        it.createIndex(Document("memberIdToMember.$**", 1))
    }

    override fun getGroupsForMember(userId: String): Map<String, Group> {
        val filter = Document("memberIdToMember.$userId", Document("\$exists", true))
        return collection.find(filter).map { it.toGroup() }.toList().associateBy { it.id }
    }

    override fun getGroupById(groupId: String): Group? {
        val doc = collection.find(Filters.eq("_id", groupId)).firstOrNull()
        return doc?.toGroup()
    }

    override fun createGroup(groupName: String): Group {
        val group = Group(
            id = uuidProvider(),
            name = groupName,
            memberIdToMember = emptyMap(),
            version = 0L
        )
        collection.insertOne(group.toDocument())
        return group
    }

    override fun updateWithOptimisticLocking(
        groupId: String,
        maxRetries: Int,
        update: (Group) -> Group
    ): Group {
        var attempt = 0

        while (attempt < maxRetries) {
            val group = this.getGroupById(groupId)
                ?: throw IllegalStateException("Group $groupId not found")

            val updatedGroup = update(group)
            val success = this.updateGroup(updatedGroup)

            if (success) return updatedGroup.copy(version = updatedGroup.version + 1)

            attempt++
        }

        throw OptimisticLockingException(
            "Failed to update group $groupId after $maxRetries attempts due to version conflict"
        )
    }

    private fun updateGroup(group: Group): Boolean {
        val currentVersion = group.version
        val newVersion = currentVersion + 1
        val filter = Filters.and(
            Filters.eq("_id", group.id),
            Filters.eq("version", currentVersion)
        )
        val updatedDoc = group.copy(version = newVersion).toDocument()
        val result = collection.replaceOne(filter, updatedDoc)
        return result.matchedCount > 0
    }

    override fun deleteGroup(groupId: String) {
        val filter = Document("_id", groupId)
        collection.deleteOne(filter)
    }

    override fun getGroupByInviteCode(inviteCode: String): Group? {
        val filter = Document("inviteCodes.code", inviteCode)
        return collection.find(filter).firstOrNull()?.toGroup()
    }


    override fun close() {
        mongoClient.close()
    }

    private fun Group.toDocument(): Document = Document().apply {
        put("_id", this@toDocument.id)
        put("name", this@toDocument.name)
        put("version", this@toDocument.version)
        put("memberIdToMember", this@toDocument.memberIdToMember.mapValues { (_, member) ->
            mapOf(
                "id" to member.id,
                "username" to member.username
            )
        })
    }

    @Suppress("UNCHECKED_CAST")
    private fun Document.toGroup(): Group {
        val members = (this["memberIdToMember"] as Map<String, Map<String, String>>).mapValues { (_, v) ->
            Group.Member(
                id = v["id"]!!,
                username = v["username"]!!
            )
        }

        return Group(
            id = this["_id"] as String,
            name = this["name"] as String,
            memberIdToMember = members,
            version = (this["version"] as Number).toLong()
        )
    }
}