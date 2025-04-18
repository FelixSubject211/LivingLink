package felix.livinglink.common

import org.ktorm.database.Database

object DatabaseInitializer {

    fun initialize(database: Database) {
        database.useTransaction {
            createUsersTable(database)
            createRefreshTokensTable(database)
        }
    }

    private fun createUsersTable(database: Database) {
        executeSql(
            database, """
            CREATE TABLE IF NOT EXISTS users (
                id VARCHAR(36) PRIMARY KEY,
                username VARCHAR(255) UNIQUE NOT NULL,
                hashed_password VARCHAR(255) NOT NULL
            )
        """
        )
    }

    private fun createRefreshTokensTable(database: Database) {
        executeSql(
            database, """
            CREATE TABLE IF NOT EXISTS refresh_tokens (
                token VARCHAR(255) PRIMARY KEY,
                user_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                username VARCHAR(36) NOT NULL,
                expires_at BIGINT NOT NULL
            )
        """
        )
    }

    private fun executeSql(database: Database, sql: String) {
        database.useConnection { conn ->
            conn.createStatement().use { stmt ->
                stmt.execute(sql)
            }
        }
    }
}