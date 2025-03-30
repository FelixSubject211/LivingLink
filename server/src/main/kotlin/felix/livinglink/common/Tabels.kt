package felix.livinglink.common

import org.ktorm.schema.Table
import org.ktorm.schema.long
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