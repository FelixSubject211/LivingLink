package felix.livinglink.change

import felix.livinglink.common.ServerConfig
import felix.livinglink.common.UuidFactory
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.RedisClient
import io.lettuce.core.api.coroutines
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import kotlinx.coroutines.runBlocking

interface ChangeNotifier {
    fun markGroupChangeForUser(userId: String)
    suspend fun getLastChangeIdForUser(userId: String): String?
}

class ChangeDefaultNotifier(
    private val config: ServerConfig,
    private val uuidFactory: UuidFactory
) : ChangeNotifier {

    private val client = RedisClient.create(config.redisUri)

    @OptIn(ExperimentalLettuceCoroutinesApi::class)
    private val redis: RedisCoroutinesCommands<String, String> = client.connect().coroutines()

    @OptIn(ExperimentalLettuceCoroutinesApi::class)
    override fun markGroupChangeForUser(userId: String) {
        runBlocking {
            redis.set("user:$userId:lastChangeId", uuidFactory())
        }
    }

    @OptIn(ExperimentalLettuceCoroutinesApi::class)
    override suspend fun getLastChangeIdForUser(userId: String): String? {
        return redis.get("user:$userId:lastChangeId")
    }
}