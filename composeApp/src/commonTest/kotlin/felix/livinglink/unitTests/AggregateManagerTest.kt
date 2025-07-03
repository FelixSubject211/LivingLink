package felix.livinglink.unitTests

import app.cash.turbine.test
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import felix.livinglink.TestAggregate
import felix.livinglink.TestData
import felix.livinglink.TestEvent
import felix.livinglink.common.model.RepositoryState
import felix.livinglink.event.eventbus.EventBus
import felix.livinglink.eventSourcing.repository.AggregateManager
import felix.livinglink.eventSourcing.repository.AggregateSnapshot
import felix.livinglink.eventSourcing.store.AggregateStore
import felix.livinglink.eventSourcing.store.EventStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AggregateManagerTest {
    private lateinit var mockEventStore: EventStore
    private lateinit var mockAggregateStore: AggregateStore
    private lateinit var mockEventBus: EventBus

    @BeforeTest
    fun setup() {
        mockEventStore = mock(mode = MockMode.autofill)
        mockAggregateStore = mock(mode = MockMode.autofill)
        mockEventBus = mock(mode = MockMode.autofill)

        everySuspend {
            mockEventStore.getEventsSince(any(), any())
        } returns emptyList()
    }

    @Test
    fun `should emit loading then empty state when no stored or incoming events`() = runTest {
        val sut = AggregateManager(
            groupId = TestData.group1.id,
            aggregationKey = TestAggregate::class.qualifiedName!!,
            payloadType = TestEvent::class,
            initial = TestAggregate(emptyList()),
            eventStore = mockEventStore,
            aggregateStore = mockAggregateStore,
            eventBus = mockEventBus,
            incomingEvents = emptyFlow(),
            scope = CoroutineScope(Dispatchers.Default)
        )

        sut.output.test {
            assertEquals(RepositoryState.Loading(null), awaitItem())
        }
    }

    @Test
    fun `should apply incoming events in batches and update state accordingly`() = runTest {

        val event1and2 = listOf(
            TestData.event1FromGroup1,
            TestData.event2FromGroup1
        )

        val event3 = listOf(
            TestData.event3FromGroup1
        )

        val incomingEvents = flowOf(
            event1and2, event3
        )

        val sut = AggregateManager(
            groupId = TestData.group1.id,
            aggregationKey = TestAggregate::class.qualifiedName!!,
            payloadType = TestEvent::class,
            initial = TestAggregate(emptyList()),
            eventStore = mockEventStore,
            aggregateStore = mockAggregateStore,
            eventBus = mockEventBus,
            incomingEvents = incomingEvents,
            scope = CoroutineScope(Dispatchers.Default)
        )

        sut.output.test {
            assertEquals(RepositoryState.Loading(data = null), awaitItem())
            assertEquals(
                RepositoryState.Loading(TestAggregate(events = event1and2)),
                awaitItem()
            )
            assertEquals(
                RepositoryState.Data(TestAggregate(events = event1and2)),
                awaitItem()
            )
            assertEquals(
                RepositoryState.Data(
                    TestAggregate(events = event1and2 + event3)
                ),
                awaitItem()
            )
        }
    }

    @Test
    fun `should load snapshot from aggregate store and apply new events from event store`() =
        runTest {
            val storedSnapshot = TestAggregate(events = listOf(TestData.event1FromGroup1))

            everySuspend {
                mockAggregateStore.get<TestAggregate>(any(), any())
            } returns AggregateSnapshot(
                aggregate = storedSnapshot,
                lastSeenGlobalEventId = TestData.event1FromGroup1.eventId
            )

            everySuspend {
                mockEventStore.getEventsSince(any(), any())
            } returns listOf(TestData.event2FromGroup1)

            val sut = AggregateManager(
                groupId = TestData.group1.id,
                aggregationKey = TestAggregate::class.qualifiedName!!,
                payloadType = TestEvent::class,
                initial = TestAggregate(emptyList()),
                eventStore = mockEventStore,
                aggregateStore = mockAggregateStore,
                eventBus = mockEventBus,
                incomingEvents = emptyFlow(),
                scope = CoroutineScope(Dispatchers.Default)
            )

            sut.output.test {
                assertEquals(
                    RepositoryState.Loading(
                        TestAggregate(
                            events = listOf(
                                TestData.event1FromGroup1,
                                TestData.event2FromGroup1
                            )
                        )
                    ),
                    awaitItem()
                )
            }
        }
}