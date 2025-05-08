package felix.livinglink.common

import felix.livinglink.Config
import io.github.cdimascio.dotenv.dotenv


interface ServerConfig : Config {
    val secret: String
    val issuer: String
    val jwtAudience: String
    val postgresJdbcUrl: String
    val postgresUsername: String
    val postgresPassword: String
    val redisUri: String
}

fun defaultServerConfig(config: Config): ServerConfig {
    val dotenv = dotenv()

    return object : ServerConfig, Config by config {
        override val secret = dotenv["JWT_SECRET"]
            ?: error("Missing env variable: JWT_SECRET")
        override val issuer = dotenv["JWT_ISSUER"]
            ?: error("Missing env variable: JWT_ISSUER")
        override val jwtAudience = dotenv["JWT_AUDIENCE"]
            ?: error("Missing env variable: JWT_AUDIENCE")
        override val postgresJdbcUrl = dotenv["POSTGRES_JDBC_URL"]
            ?: error("Missing env variable: POSTGRES_JDBC_URL")
        override val postgresUsername = dotenv["POSTGRES_USER"]
            ?: error("Missing env variable: POSTGRES_USER")
        override val postgresPassword = dotenv["POSTGRES_PASSWORD"]
            ?: error("Missing env variable: POSTGRES_PASSWORD")
        override val redisUri = dotenv["REDIS_URI"]
            ?: error("Missing env variable: REDIS_URI")
    }
}
