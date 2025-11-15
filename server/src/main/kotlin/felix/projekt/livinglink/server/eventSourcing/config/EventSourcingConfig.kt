package felix.projekt.livinglink.server.eventSourcing.config

import io.github.cdimascio.dotenv.dotenv

interface EventSourcingConfig {
    val postgresHost: String
    val postgresPort: Int
    val postgresDatabase: String
    val postgresUsername: String
    val postgresPassword: String
    val maxPoolSize: Int
    val minIdleConnections: Int
    val maxPoolLifetimeMillis: Long
    val pollPageSize: Int
    val defaultPollAfterMillis: Long
    val notModifiedPollAfterMillis: Long
}

fun eventSourcingDefaultConfig(): EventSourcingConfig {
    val dotenv = dotenv()

    return object : EventSourcingConfig {
        override val postgresHost: String = dotenv["EVENT_SOURCING_POSTGRES_HOST"]
        override val postgresPort: Int = dotenv["EVENT_SOURCING_POSTGRES_PORT"].toInt()
        override val postgresDatabase: String = dotenv["EVENT_SOURCING_POSTGRES_DATABASE"]
        override val postgresUsername: String = dotenv["EVENT_SOURCING_POSTGRES_USERNAME"]
        override val postgresPassword: String = dotenv["EVENT_SOURCING_POSTGRES_PASSWORD"]
        override val maxPoolSize: Int = dotenv["EVENT_SOURCING_POSTGRES_MAX_POOL_SIZE"].toInt()
        override val minIdleConnections: Int = dotenv["EVENT_SOURCING_POSTGRES_MIN_IDLE"].toInt()
        override val maxPoolLifetimeMillis: Long = dotenv["EVENT_SOURCING_POSTGRES_MAX_LIFETIME_MILLIS"].toLong()
        override val pollPageSize: Int = dotenv["EVENT_SOURCING_POLL_PAGE_SIZE"].toInt()
        override val defaultPollAfterMillis: Long = dotenv["EVENT_SOURCING_POLL_AFTER_DEFAULT_MILLIS"].toLong()
        override val notModifiedPollAfterMillis: Long =
            dotenv["EVENT_SOURCING_POLL_AFTER_NOT_MODIFIED_MILLIS"].toLong()
    }
}
