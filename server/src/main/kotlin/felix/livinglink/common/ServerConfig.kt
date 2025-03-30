package felix.livinglink.common

import felix.livinglink.Config
import io.github.cdimascio.dotenv.dotenv


interface ServerConfig : Config {
    val secret: String
    val issuer: String
    val jwtAudience: String
    val dbJdbcUrl: String
    val dbUsername: String
    val dbPassword: String
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
        override val dbJdbcUrl = dotenv["JDBC_URL"]
            ?: error("Missing env variable: JDBC_URL")
        override val dbUsername = dotenv["DB_USER"]
            ?: error("Missing env variable: DB_USER")
        override val dbPassword = dotenv["DB_PASSWORD"]
            ?: error("Missing env variable: DB_PASSWORD")
    }
}
