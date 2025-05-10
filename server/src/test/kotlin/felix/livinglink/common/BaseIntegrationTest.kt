package felix.livinglink.common

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.cdimascio.dotenv.dotenv
import io.lettuce.core.RedisClient
import io.lettuce.core.api.sync.RedisCommands
import org.junit.Before
import org.ktorm.database.Database

abstract class BaseIntegrationTest {

    val dotenv = dotenv()

    val config: ServerConfig = object : ServerConfig {
        override val serverHost: String = "serverHost"
        override val serverPort: Int = 8080
        override val authenticationConfig = "authenticationConfig"
        override val userIdClaim = "userIdClaim"
        override val usernameClaim = "usernameClaim"
        override val sessionIdClaim = "sessionIdClaim"
        override val groupIdsClaim = "groupIdsClaim"
        override val accessTokenExpirationMs = 1_000_000
        override val refreshTokenExpirationMs = 1_000_000_000
        override val pollingIntervalSeconds = 5
        override val pollingRetryDelaySeconds = 10
        override val secret = "secret"
        override val issuer = "issuer"
        override val jwtAudience = "jwtAudience"
        override val postgresJdbcUrl = dotenv["POSTGRES_JDBC_TEST_URL"]
        override val postgresUsername = dotenv["POSTGRES_TEST_USER"]
        override val postgresPassword = dotenv["POSTGRES_TEST_PASSWORD"]
        override val redisUri = dotenv["REDIS_TEST_URI"]
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
        database.dropTableIfExists(EventCountersTable)
        database.dropTableIfExists(EventSourcingEventsTable)
        database.dropTableIfExists(RefreshTokensTable)
        database.dropTableIfExists(GroupMembersTable)
        database.dropTableIfExists(GroupInvitesTable)
        database.dropTableIfExists(GroupsTable)
        database.dropTableIfExists(UsersTable)

        redis.flushall()
    }

    private fun hikariDataSource(config: ServerConfig): HikariDataSource {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = config.postgresJdbcUrl
            username = config.postgresUsername
            password = config.postgresPassword
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
