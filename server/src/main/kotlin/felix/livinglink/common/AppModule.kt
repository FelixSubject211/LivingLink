package felix.livinglink.common

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import felix.livinglink.auth.AuthService
import felix.livinglink.auth.JwtDefaultService
import felix.livinglink.auth.JwtService
import felix.livinglink.auth.PasswordHasherDefaultService
import felix.livinglink.auth.PasswordHasherService
import felix.livinglink.auth.PostgresUserStore
import felix.livinglink.auth.UserStore
import felix.livinglink.groups.GroupDefaultStore
import felix.livinglink.groups.GroupService
import felix.livinglink.groups.GroupStore
import org.ktorm.database.Database

interface AppModule {
    val database: Database
    val timeService: TimeService
    val uuidFactory: UuidFactory
    val userStore: UserStore
    val passwordHasherService: PasswordHasherService
    val jwtService: JwtService
    val authService: AuthService
    val groupStore: GroupStore
    val groupService: GroupService
}

fun defaultAppModule(
    config: ServerConfig,
    timeService: TimeService = TimeDefaultService(),
    uuidFactory: UuidFactory = UuidDefaultFactory()
): AppModule {
    val database = DataBaseProvider.getDatabase(config)

    return object : AppModule {
        override val database = database
        override val timeService = timeService
        override val uuidFactory = uuidFactory
        override val userStore = PostgresUserStore(database = database)
        override val passwordHasherService = PasswordHasherDefaultService()
        override val jwtService = JwtDefaultService(
            config = config,
            timeService = timeService,
            uuidFactory = uuidFactory
        )
        override val authService = AuthService(
            userStore = userStore,
            passwordHasherService = passwordHasherService,
            jwtService = jwtService,
            timeService = timeService,
            uuidFactory = uuidFactory
        )
        override val groupStore = GroupDefaultStore(
            timeService = timeService,
            uuidFactory = uuidFactory,
            database = database
        )
        override val groupService = GroupService(
            groupStore = groupStore
        )
    }
}

/**
 * In production, this ensures a single shared connection pool.
 * In tests, it prevents multiple pools and avoids "too many clients" errors.
 */
object DataBaseProvider {
    private var _database: Database? = null

    fun getDatabase(config: ServerConfig): Database {
        return _database ?: createAndStoreDatabase(config)
    }

    private fun createAndStoreDatabase(config: ServerConfig): Database {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = config.dbJdbcUrl
            username = config.dbUsername
            password = config.dbPassword
            driverClassName = "org.postgresql.Driver"
            isAutoCommit = true
        }
        val hikariDataSource = HikariDataSource(hikariConfig)
        val db = Database.connect(dataSource = hikariDataSource)
        _database = db
        return db
    }
}