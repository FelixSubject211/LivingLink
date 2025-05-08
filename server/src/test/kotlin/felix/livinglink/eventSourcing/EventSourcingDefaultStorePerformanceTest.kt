package felix.livinglink.eventSourcing

import felix.livinglink.common.BaseIntegrationTest
import felix.livinglink.common.DatabaseInitializer
import felix.livinglink.common.MockTimeService
import felix.livinglink.common.RawUser
import felix.livinglink.common.addSampleGroups
import felix.livinglink.common.addSampleUsers
import felix.livinglink.common.defaultAppModule
import felix.livinglink.group.Group
import felix.livinglink.module
import io.ktor.server.testing.testApplication
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals

class EventSourcingDefaultStorePerformanceTest : BaseIntegrationTest() {

    private val writeEventCount = 1_000
    private val readEventCount = 10_000
    private val threadCount = 10
    private val writeEventsPerThread = writeEventCount / threadCount
    private val eventType = "event"
    private val payload = """{"some":"data"}"""

    @Test
    fun `should append $writeEventCount events in parallel into one group`() = testApplication {
        DatabaseInitializer.initialize(database)

        application {
            module(config = config, appModule = defaultAppModule(config = config))
        }

        val now = Clock.System.now()
        val group = Group(
            id = "sharedGroup",
            name = "Shared Group",
            groupMemberIdsToName = emptyMap(),
            createdAt = now
        )
        val users = (1..threadCount).map {
            RawUser(id = "user$it", username = "user$it", password = "pw")
        }

        database.addSampleGroups(group = group)
        database.addSampleUsers(users)

        val store = EventSourcingDefaultStore(database, MockTimeService)

        val start = System.currentTimeMillis()

        val threads = users.map { user ->
            Thread {
                repeat(writeEventsPerThread) {
                    store.appendEvent(group.id, user.id, eventType, payload)
                        ?: error("Failed to insert for user ${user.id}")
                }
            }
        }

        threads.forEach { it.start() }
        threads.forEach { it.join() }

        val duration = System.currentTimeMillis() - start
        val events = store.getEventsSince(group.id, 0)

        assertEquals(
            writeEventCount,
            events.size,
            "Expected $writeEventCount events in shared group"
        )

        println("Shared group: $writeEventCount events via $threadCount threads in $duration ms")
        println("Average per event: %.2f ms".format(duration / writeEventCount.toDouble()))
    }

    @Test
    fun `should append $writeEventCount events in parallel across $threadCount groups`() =
        testApplication {
            DatabaseInitializer.initialize(database)

            application {
                module(config = config, appModule = defaultAppModule(config = config))
            }

            val now = Clock.System.now()
            val groups = (1..threadCount).map {
                Group(
                    id = "group$it",
                    name = "Group $it",
                    groupMemberIdsToName = emptyMap(),
                    createdAt = now
                )
            }
            val users = (1..threadCount).map {
                RawUser(id = "user$it", username = "user$it", password = "pw")
            }

            database.addSampleGroups(groups)
            database.addSampleUsers(users)

            val store = EventSourcingDefaultStore(database, MockTimeService)

            val start = System.currentTimeMillis()

            val threads = groups.mapIndexed { index, group ->
                val user = users[index]
                Thread {
                    repeat(writeEventsPerThread) {
                        store.appendEvent(group.id, user.id, eventType, payload)
                            ?: error("Failed to insert for group ${group.id}")
                    }
                }
            }

            threads.forEach { it.start() }
            threads.forEach { it.join() }

            val duration = System.currentTimeMillis() - start

            groups.forEach {
                val events = store.getEventsSince(it.id, 0)
                assertEquals(
                    writeEventsPerThread,
                    events.size,
                    "Expected $writeEventsPerThread events in group ${it.id}"
                )
            }

            println("Multi-group: $writeEventCount events via $threadCount threads in $duration ms")
            println("Average per event: %.2f ms".format(duration / writeEventCount.toDouble()))
        }

    @Test
    fun `should read $readEventCount events in one bulk read`() = testApplication {
        prepareSharedGroupWithEvents(readEventCount)

        val store = EventSourcingDefaultStore(database, MockTimeService)
        val start = System.currentTimeMillis()

        val events = store.getEventsSince("sharedGroup", 0L)

        val duration = System.currentTimeMillis() - start
        assertEquals(readEventCount, events.size)

        println("One-time read: $readEventCount events in $duration ms")
        println("Average per event: %.2f ms".format(duration / readEventCount.toDouble()))
    }

    private fun prepareSharedGroupWithEvents(eventCount: Int) {
        DatabaseInitializer.initialize(database)

        val group = Group(
            id = "sharedGroup",
            name = "Shared Group",
            groupMemberIdsToName = emptyMap(),
            createdAt = Clock.System.now()
        )
        val user = RawUser(id = "user", username = "user", password = "pw")

        database.addSampleGroups(group = group)
        database.addSampleUsers(user = user)

        val store = EventSourcingDefaultStore(database, MockTimeService)

        database.useTransaction {
            repeat(eventCount) {
                store.appendEvent(group.id, user.id, eventType, payload)
                    ?: error("Failed at $it")
            }
        }
    }
}