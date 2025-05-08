package felix.livinglink.common

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.lettuce.core.RedisClient
import io.lettuce.core.api.sync.RedisCommands
import org.junit.Before
import org.ktorm.database.Database

abstract class BaseIntegrationTest {

    val config: ServerConfig = object : ServerConfig {
        override val serverHost: String = "serverHost"
        override val serverPort: Int = 8080
        override val authenticationConfig = "authenticationConfig"
        override val userIdClaim = "userIdClaim"
        override val usernameClaim = "usernameClaim"
        override val sessionIdClaim = "sessionIdClaim"
        override val accessTokenExpirationMs = 1_000_000
        override val refreshTokenExpirationMs = 1_000_000_000
        override val secret = "secret"
        override val issuer = "issuer"
        override val jwtAudience = "jwtAudience"
        override val dbJdbcUrl = "jdbc:postgresql://localhost:5433/test_postgres"
        override val dbUsername = "test_postgres"
        override val dbPassword = "test_postgres"
        override val redisUri = "redis://localhost:6380"
    }

    val database = Database.connect(hikariDataSource(config))

    private val redis: RedisCommands<String, String> by lazy {
        RedisClient.create(config.redisUri).connect().sync()
    }

    protected fun assertRedisChangeSet(userId: String, expectedChangeId: String) {
        val key = "user:$userId:lastGroupChangeId"
        val value = redis.get(key)
        check(value == expectedChangeId) {
            "Expected changeId for user '$userId' to be '$expectedChangeId', but was '$value'"
        }
    }

    protected fun assertNoRedisChangeSet(userId: String) {
        val key = "user:$userId:lastChangeId"
        val value = redis.get(key)
        check(value.isNullOrBlank()) {
            "Expected no changeId for user '$userId', but got: $value"
        }
    }

    @Before
    fun setup() {
        database.dropTableIfExists(RefreshTokensTable)
        database.dropTableIfExists(GroupMembersTable)
        database.dropTableIfExists(GroupInvitesTable)
        database.dropTableIfExists(GroupsTable)
        database.dropTableIfExists(UsersTable)

        redis.flushall()
    }

    private fun hikariDataSource(config: ServerConfig): HikariDataSource {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = config.dbJdbcUrl
            username = config.dbUsername
            password = config.dbPassword
            driverClassName = "org.postgresql.Driver"
            isAutoCommit = true
            maximumPoolSize = 5
            minimumIdle = 1
            idleTimeout = 10_000
            maxLifetime = 30_000
            leakDetectionThreshold = 10_000
            connectionTimeout = 30_000
        }
        return HikariDataSource(hikariConfig)
    }
}
