package felix.projekt.livinglink.server.groups.infrastructure

import felix.projekt.livinglink.server.groups.config.GroupsConfig
import felix.projekt.livinglink.server.groups.domain.GroupVersionCache
import io.lettuce.core.RedisClient
import io.lettuce.core.ScriptOutputType
import io.lettuce.core.api.async.RedisAsyncCommands
import kotlinx.coroutines.future.await

class GroupVersionRedisCache(
    private val groupsConfig: GroupsConfig
) : GroupVersionCache {

    private val redisClient = RedisClient.create(groupsConfig.groupRedisUri)
    private val connection: RedisAsyncCommands<String, String> = redisClient.connect().async()

    private val emptyPlaceholder = "__empty__"

    override suspend fun setGroupVersions(userId: String, versions: GroupVersionCache.GroupVersions) {
        val args = versions.groupIdsToGroupVersion.flatMap { listOf(it.key, it.value.toString()) }.toTypedArray()

        val luaScript = if (args.isNotEmpty()) {
            """
            redis.call('DEL', KEYS[1])
            for i = 1, #ARGV, 2 do
                redis.call('HSET', KEYS[1], ARGV[i], tonumber(ARGV[i+1]))
            end
            return 1
            """.trimIndent()
        } else {
            """
            if redis.call('EXISTS', KEYS[1]) == 0 then
                redis.call('HSET', KEYS[1], '${emptyPlaceholder}', 0)
            end
            return 1
            """.trimIndent()
        }

        connection.eval<Long>(
            luaScript,
            ScriptOutputType.INTEGER,
            arrayOf(userId),
            *args
        ).await()

        connection.expire(userId, groupsConfig.cacheLifetimeSeconds).await()
    }

    override suspend fun getGroupVersions(userId: String): GroupVersionCache.GroupVersions? {
        val exists = connection.exists(userId).await() > 0
        if (!exists) {
            return null
        }

        val entries = connection.hgetall(userId).await()
        val map = if (entries.containsKey(emptyPlaceholder)) {
            emptyMap()
        } else {
            entries.mapValues { it.value.toLong() }
        }

        return GroupVersionCache.GroupVersions(map)
    }

    override suspend fun addOrUpdateGroupVersionIfUserExists(userId: String, groupId: String, version: Long) {
        val luaScript = """
            if redis.call("EXISTS", KEYS[1]) == 1 then
                redis.call("HDEL", KEYS[1], '${emptyPlaceholder}')
                redis.call("HSET", KEYS[1], ARGV[1], tonumber(ARGV[2]))
                return 1
            else
                return 0
            end
        """.trimIndent()

        connection.eval<Long>(
            luaScript,
            ScriptOutputType.INTEGER,
            arrayOf(userId),
            groupId,
            version.toString()
        ).await()

        connection.expire(userId, groupsConfig.cacheLifetimeSeconds).await()
    }

    override suspend fun deleteGroupVersions(userId: String) {
        connection.del(userId).await()
    }
}