package felix.livinglink.common

import org.ktorm.database.Database

object DatabaseInitializer {

    fun initialize(database: Database) {
        database.useTransaction {
            createUsersTable(database)
            createRefreshTokensTable(database)
            createGroupsTable(database)
            createGroupMembersTable(database)
            createGroupInvitesTable(database)
            createEventsTable(database)
            createEventCountersTable(database)
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

    private fun createGroupsTable(database: Database) {
        executeSql(
            database, """
            CREATE TABLE IF NOT EXISTS groups (
                id VARCHAR(36) PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                created_at TIMESTAMP NOT NULL
            )
        """
        )
    }

    private fun createGroupMembersTable(database: Database) {
        executeSql(
            database, """
            CREATE TABLE IF NOT EXISTS group_members (
                group_id VARCHAR(36) NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
                user_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                is_admin BOOLEAN NOT NULL DEFAULT FALSE,
                created_at TIMESTAMP NOT NULL,
                PRIMARY KEY (group_id, user_id)
            )
        """
        )
    }

    private fun createGroupInvitesTable(database: Database) {
        executeSql(
            database, """
            CREATE TABLE IF NOT EXISTS group_invites (
            code VARCHAR(255) PRIMARY KEY,
            group_id VARCHAR(36) NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
            created_by VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
            created_at TIMESTAMP NOT NULL
            )
        """
        )
    }

    private fun createEventsTable(database: Database) {
        executeSql(
            database, """
            CREATE TABLE IF NOT EXISTS event_sourcing_events (
            group_id VARCHAR(36) NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
            event_id BIGINT NOT NULL,
            event_type VARCHAR(255) NOT NULL,
            user_id VARCHAR(36),
            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            payload TEXT NOT NULL,
            PRIMARY KEY (group_id, event_id)
            );
        """
        )

        executeSql(
            database, """
            CREATE INDEX IF NOT EXISTS idx_events_by_group_and_event_id
            ON event_sourcing_events (group_id, event_id);
        """
        )
    }

    private fun createEventCountersTable(database: Database) {
        executeSql(
            database, """
            CREATE TABLE IF NOT EXISTS event_counters (
                group_id VARCHAR(36) PRIMARY KEY REFERENCES groups(id) ON DELETE CASCADE,
                last_event_id BIGINT NOT NULL
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