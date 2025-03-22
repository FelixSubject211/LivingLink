package felix.livinglink.auth

import felix.livinglink.common.RefreshTokensTable
import felix.livinglink.common.UsersTable
import org.ktorm.database.Database
import org.ktorm.dsl.delete
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.insert
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.where

interface UserStore {
    fun addUser(userId: String, username: String, hashedPassword: String): String?
    fun getUserByUsername(username: String): User?
    fun storeRefreshToken(userId: String, refreshToken: RefreshToken): Boolean
    fun getRefreshToken(token: String): RefreshToken?
    fun deleteRefreshToken(token: String): Boolean
    fun deleteUser(userId: String): Boolean
}

class PostgresUserStore(
    private val database: Database
) : UserStore {

    override fun addUser(userId: String, username: String, hashedPassword: String): String? {
        return try {
            val rowsInserted = database.insert(UsersTable) {
                set(it.id, userId)
                set(it.username, username)
                set(it.hashedPassword, hashedPassword)
            }
            if (rowsInserted > 0) userId else null
        } catch (e: Exception) {
            null
        }
    }

    override fun getUserByUsername(username: String): User? {
        return database
            .from(UsersTable)
            .select(UsersTable.id, UsersTable.username, UsersTable.hashedPassword)
            .where { UsersTable.username eq username }
            .map { row ->
                User(
                    id = row[UsersTable.id]!!,
                    username = row[UsersTable.username]!!,
                    hashedPassword = row[UsersTable.hashedPassword]!!
                )
            }
            .firstOrNull()
    }

    override fun storeRefreshToken(userId: String, refreshToken: RefreshToken): Boolean {
        return try {
            val rowsInserted = database.insert(RefreshTokensTable) {
                set(it.token, refreshToken.token)
                set(it.userId, userId)
                set(it.username, refreshToken.username)
                set(it.expiresAt, refreshToken.expiresAt)
            }
            return rowsInserted > 0
        } catch (e: Exception) {
            false
        }
    }

    override fun getRefreshToken(token: String): RefreshToken? {
        return database
            .from(RefreshTokensTable)
            .select(
                RefreshTokensTable.token,
                RefreshTokensTable.userId,
                RefreshTokensTable.username,
                RefreshTokensTable.expiresAt
            )
            .where { RefreshTokensTable.token eq token }
            .map { row ->
                RefreshToken(
                    token = row[RefreshTokensTable.token]!!,
                    userId = row[RefreshTokensTable.userId]!!,
                    username = row[RefreshTokensTable.username]!!,
                    expiresAt = row[RefreshTokensTable.expiresAt]!!
                )
            }
            .firstOrNull()
    }

    override fun deleteRefreshToken(token: String): Boolean {
        return try {
            val rowsDeleted = database.delete(RefreshTokensTable) {
                it.token eq token
            }
            return rowsDeleted > 0
        } catch (e: Exception) {
            false
        }
    }

    override fun deleteUser(userId: String): Boolean {
        return try {
            val rowsDeleted = database.delete(UsersTable) {
                it.id eq userId
            }
            rowsDeleted > 0
        } catch (e: Exception) {
            false
        }
    }
}