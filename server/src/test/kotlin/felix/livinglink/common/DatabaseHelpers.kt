package felix.livinglink.common

import felix.livinglink.auth.RefreshToken
import org.ktorm.database.Database
import org.ktorm.dsl.from
import org.ktorm.dsl.insert
import org.ktorm.dsl.select
import org.ktorm.schema.BaseTable
import org.mindrot.jbcrypt.BCrypt
import kotlin.test.assertEquals

data class RawUser(
    val id: String,
    val username: String,
    val password: String
)

fun Database.assertHasTotalRecords(
    vararg tables: BaseTable<*>,
    totalRecords: Int
) {
    tables.forEach { table ->
        assertEquals(
            expected = totalRecords,
            actual = this.from(table).select().totalRecordsInAllPages,
            message = "Expected $totalRecords records in table ${table.tableName}, but found a different count."
        )
    }
}

fun Database.dropTableIfExists(table: BaseTable<*>) {
    this.useConnection { conn ->
        val stmt = conn.createStatement()
        stmt.executeUpdate("DROP TABLE IF EXISTS ${table.tableName}")
    }
}

fun Database.addSampleData(
    users: Collection<RawUser> = emptySet(),
    refreshTokens: Collection<RefreshToken> = emptySet(),
    user: RawUser? = null,
    refreshToken: RefreshToken? = null
) {
    DatabaseInitializer.initialize(this)

    val allUsers = buildList {
        addAll(users)
        user?.let { add(it) }
    }

    allUsers.forEach { singleUser ->
        insert(UsersTable) {
            set(it.id, singleUser.id)
            set(it.username, singleUser.username)
            set(it.hashedPassword, BCrypt.hashpw(singleUser.password, BCrypt.gensalt()))
        }
    }

    val allRefreshTokens = buildList {
        addAll(refreshTokens)
        refreshToken?.let { add(it) }
    }

    allRefreshTokens.forEach { singleRefreshToken ->
        insert(RefreshTokensTable) {
            set(it.token, singleRefreshToken.token)
            set(it.userId, singleRefreshToken.userId)
            set(it.username, singleRefreshToken.username)
            set(it.expiresAt, singleRefreshToken.expiresAt)
        }
    }
}