package felix.projekt.livinglink.server.groups.config

import io.github.cdimascio.dotenv.dotenv

interface GroupsConfig {
    val mongoHost: String
    val mongoPort: Int
    val mongoDatabase: String
    val mongoUsername: String
    val mongoPassword: String
    val groupRedisUri: String
    val defaultPollAfterMillis: Long
    val notModifiedPollAfterMillis: Long
    val cacheLifetimeSeconds: Long
}

fun groupsDefaultConfig(): GroupsConfig {
    val dotenv = dotenv()

    return object : GroupsConfig {
        override val mongoHost: String = dotenv["GROUPS_MONGO_HOST"]!!
        override val mongoPort: Int = dotenv["GROUPS_MONGO_PORT"]!!.toInt()
        override val mongoDatabase: String = dotenv["GROUPS_MONGO_DATABASE"]!!
        override val mongoUsername: String = dotenv["GROUPS_MONGO_ROOT_USERNAME"]!!
        override val mongoPassword: String = dotenv["GROUPS_MONGO_ROOT_PASSWORD"]!!
        override val groupRedisUri: String = dotenv["GROUPS_REDIS_URI"]!!
        override val defaultPollAfterMillis: Long = dotenv["GROUPS_POLL_AFTER_DEFAULT_MILLIS"]!!.toLong()
        override val notModifiedPollAfterMillis: Long = dotenv["GROUPS_POLL_AFTER_NOT_MODIFIED_MILLIS"]!!.toLong()
        override val cacheLifetimeSeconds: Long = dotenv["GROUPS_CACHE_LIFETIME_SECONDS"]!!.toLong()
    }
}