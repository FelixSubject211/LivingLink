package felix.livinglink.common

import org.ktorm.schema.Table
import org.ktorm.schema.boolean
import org.ktorm.schema.long
import org.ktorm.schema.text
import org.ktorm.schema.timestamp
import org.ktorm.schema.varchar

object UsersTable : Table<Nothing>("users") {
    val id = varchar("id").primaryKey()
    val username = varchar("username")
    val hashedPassword = varchar("hashed_password")
}

object RefreshTokensTable : Table<Nothing>("refresh_tokens") {
    val token = varchar("token").primaryKey()
    val userId = varchar("user_id")
    val username = varchar("username")
    val expiresAt = long("expires_at")
}

object GroupsTable : Table<Nothing>("groups") {
    val id = varchar("id").primaryKey()
    val name = varchar("name")
    val createdAt = timestamp("created_at")
}

object GroupMembersTable : Table<Nothing>("group_members") {
    val groupId = varchar("group_id")
    val userId = varchar("user_id")
    val isAdmin = boolean("is_admin")
    val createdAt = timestamp("created_at")
}

object GroupInvitesTable : Table<Nothing>("group_invites") {
    val code = varchar("code").primaryKey()
    val groupId = varchar("group_id")
    val createdBy = varchar("created_by")
    val createdAt = timestamp("created_at")
}

object EventSourcingEventsTable : Table<Nothing>("event_sourcing_events") {
    val groupId = varchar("group_id")
    val eventId = long("event_id") // sequential per group
    val eventType = varchar("event_type")
    val userId = varchar("user_id")
    val createdAt = timestamp("created_at")
    val payload = text("payload")
}

object EventCountersTable : Table<Nothing>("event_counters") {
    val groupId = varchar("group_id")
    val lastEventId = long("last_event_id")
}