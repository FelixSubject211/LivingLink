package felix.projekt.livinglink.server.eventSourcing.infrastructure

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import felix.projekt.livinglink.server.eventSourcing.config.EventSourcingConfig
import felix.projekt.livinglink.server.eventSourcing.domain.EventSourcingEvent
import felix.projekt.livinglink.server.eventSourcing.domain.EventSourcingRepository
import felix.projekt.livinglink.shared.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonElement
import java.sql.Connection

class EventSourcingPostgresRepository(
    private val config: EventSourcingConfig
) : EventSourcingRepository {

    val source = HikariDataSource(
        HikariConfig().apply {
            jdbcUrl = "jdbc:postgresql://${config.postgresHost}:${config.postgresPort}/${config.postgresDatabase}"
            username = config.postgresUsername
            password = config.postgresPassword
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = config.maxPoolSize
            minimumIdle = config.minIdleConnections
            maxLifetime = config.maxPoolLifetimeMillis
        }
    )

    init {
        initializeSchema()
    }

    override suspend fun appendEvent(
        groupId: String,
        topic: String,
        createdBy: String,
        payload: JsonElement,
        expectedLastEventId: Long
    ): EventSourcingEvent? = withConnection { connection ->
        connection.autoCommit = false
        try {
            val eventId = reserveEventId(
                connection = connection,
                groupId = groupId,
                topic = topic,
                expectedLastEventId = expectedLastEventId
            ) ?: return@withConnection null

            val sql =
                """
            INSERT INTO event_sourcing_events
                (group_id, topic, event_id, created_by, payload)
            VALUES
                (?, ?, ?, ?, ?::jsonb)
            RETURNING event_id, group_id, topic, created_by, created_at, payload
            """.trimIndent()

            connection.prepareStatement(sql).use { stmt ->
                stmt.setString(1, groupId)
                stmt.setString(2, topic)
                stmt.setLong(3, eventId)
                stmt.setString(4, createdBy)
                stmt.setString(5, json.encodeToString(JsonElement.serializer(), payload))

                stmt.executeQuery().use { rs ->
                    rs.next()

                    val event = EventSourcingEvent(
                        eventId = rs.getLong("event_id"),
                        groupId = rs.getString("group_id"),
                        topic = rs.getString("topic"),
                        createdBy = rs.getString("created_by"),
                        createdAtEpochMillis = rs.getTimestamp("created_at").toInstant().toEpochMilli(),
                        payload = json.decodeFromString(JsonElement.serializer(), rs.getString("payload"))
                    )

                    connection.commit()
                    return@withConnection event
                }
            }
        } catch (e: Throwable) {
            connection.rollback()
            throw e
        } finally {
            connection.autoCommit = true
        }
    }

    override suspend fun fetchEvents(
        groupId: String,
        topic: String,
        lastKnownEventId: Long,
        limit: Int
    ): List<EventSourcingEvent> = withConnection { connection ->
        val sql = buildString {
            append(
                """
                SELECT event_id, group_id, topic, created_by, created_at, payload
                FROM event_sourcing_events
                WHERE group_id = ? AND topic = ?
                """.trimIndent()
            )
            append(" AND event_id > ?")
            append(" ORDER BY event_id ASC LIMIT ?")
        }
        connection.prepareStatement(sql).use { statement ->
            statement.setString(1, groupId)
            statement.setString(2, topic)
            var parameterIndex = 3
            statement.setLong(parameterIndex, lastKnownEventId)
            parameterIndex += 1
            statement.setInt(parameterIndex, limit)
            statement.executeQuery().use { rs ->
                val events = mutableListOf<EventSourcingEvent>()
                while (rs.next()) {
                    events += EventSourcingEvent(
                        eventId = rs.getLong("event_id"),
                        groupId = rs.getString("group_id"),
                        topic = rs.getString("topic"),
                        createdBy = rs.getString("created_by"),
                        createdAtEpochMillis = rs.getTimestamp("created_at").toInstant().toEpochMilli(),
                        payload = json.decodeFromString(JsonElement.serializer(), rs.getString("payload"))
                    )
                }
                events
            }
        }
    }

    override suspend fun totalEvents(groupId: String, topic: String): Long = withConnection { connection ->
        val sql =
            """
            SELECT last_event_id FROM event_sourcing_topic_counters
            WHERE group_id = ? AND topic = ?
            """.trimIndent()
        connection.prepareStatement(sql).use { statement ->
            statement.setString(1, groupId)
            statement.setString(2, topic)
            statement.executeQuery().use { rs ->
                return@withConnection if (rs.next()) rs.getLong("last_event_id") else 0L
            }
        }
    }

    override fun close() {
        source.close()
    }

    private fun reserveEventId(
        connection: Connection,
        groupId: String,
        topic: String,
        expectedLastEventId: Long
    ): Long? {
        val updateSql =
            """
        UPDATE event_sourcing_topic_counters
        SET last_event_id = last_event_id + 1
        WHERE group_id = ? AND topic = ? AND last_event_id = ?
        RETURNING last_event_id
        """.trimIndent()

        connection.prepareStatement(updateSql).use { stmt ->
            stmt.setString(1, groupId)
            stmt.setString(2, topic)
            stmt.setLong(3, expectedLastEventId)

            stmt.executeQuery().use { rs ->
                if (rs.next()) {
                    return rs.getLong("last_event_id")
                }
            }
        }

        if (expectedLastEventId != 0L) {
            return null
        }

        val insertSql =
            """
        INSERT INTO event_sourcing_topic_counters (group_id, topic, last_event_id)
        VALUES (?, ?, 1)
        ON CONFLICT (group_id, topic) DO NOTHING
        RETURNING last_event_id
        """.trimIndent()

        connection.prepareStatement(insertSql).use { stmt ->
            stmt.setString(1, groupId)
            stmt.setString(2, topic)

            stmt.executeQuery().use { rs ->
                if (rs.next()) {
                    return rs.getLong("last_event_id")
                }
            }
        }

        return null
    }

    private fun initializeSchema() {
        source.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.execute(
                    """
                    CREATE TABLE IF NOT EXISTS event_sourcing_events (
                        id BIGSERIAL PRIMARY KEY,
                        group_id TEXT NOT NULL,
                        topic TEXT NOT NULL,
                        event_id BIGINT NOT NULL,
                        created_by TEXT NOT NULL,
                        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                        payload JSONB NOT NULL,
                        UNIQUE (group_id, topic, event_id)
                    )
                    """.trimIndent()
                )
                statement.execute(
                    """
                    CREATE TABLE IF NOT EXISTS event_sourcing_topic_counters (
                        group_id TEXT NOT NULL,
                        topic TEXT NOT NULL,
                        last_event_id BIGINT NOT NULL,
                        PRIMARY KEY (group_id, topic)
                    )
                    """.trimIndent()
                )
                statement.execute(
                    """
                    CREATE INDEX IF NOT EXISTS event_sourcing_events_group_topic_created_idx
                    ON event_sourcing_events (group_id, topic, event_id)
                    """.trimIndent()
                )
            }
        }
    }

    private suspend fun <T> withConnection(block: suspend (Connection) -> T): T = withContext(Dispatchers.IO) {
        source.connection.use { connection ->
            block(connection)
        }
    }
}
