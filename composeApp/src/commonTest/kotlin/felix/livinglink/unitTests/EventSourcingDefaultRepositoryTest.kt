package felix.livinglink.unitTests

import app.cash.turbine.test
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import felix.livinglink.TestData
import felix.livinglink.common.model.LivingLinkResult
import felix.livinglink.common.model.RepositoryState
import felix.livinglink.event.eventbus.EventBus
import felix.livinglink.eventSourcing.EventSourcingEvent
import felix.livinglink.eventSourcing.GetEventSourcingEventsResponse
import felix.livinglink.eventSourcing.network.EventSourcingNetworkDataSource
import felix.livinglink.eventSourcing.repository.EventSourcingDefaultRepository
import felix.livinglink.eventSourcing.store.AggregateStore
import felix.livinglink.eventSourcing.store.EventStore
import felix.livinglink.shoppingList.ShoppingListAggregate
import felix.livinglink.shoppingList.ShoppingListEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class EventSourcingDefaultRepositoryTest {
    private lateinit var mockEventSourcingNetworkDataSource: EventSourcingNetworkDataSource
    private lateinit var mockEventStore: EventStore
    private lateinit var mockAggregateStore: AggregateStore
    private lateinit var mockEventBus: EventBus
    private lateinit var sut: EventSourcingDefaultRepository

    private lateinit var eventBusEvents: MutableSharedFlow<EventBus.Event>

    private val cacheKey =
        "${TestData.group1.id}:${ShoppingListEvent::class.qualifiedName}:${ShoppingListAggregate::class.qualifiedName}"

    @BeforeTest
    fun setup() {
        mockEventSourcingNetworkDataSource = mock(mode = MockMode.autofill)
        mockEventStore = mock(mode = MockMode.autofill)
        mockAggregateStore = mock(mode = MockMode.autofill)
        mockEventBus = mock(mode = MockMode.autofill)

        eventBusEvents = MutableSharedFlow()

        every {
            mockEventBus.events
        } returns eventBusEvents

        everySuspend {
            mockEventStore.getEvents(any())
        } returns emptyList()

        everySuspend {
            mockEventSourcingNetworkDataSource.getEvents(any(), any())
        } returns LivingLinkResult.Success(
            GetEventSourcingEventsResponse(
                events = emptyList()
            )
        )

        sut = EventSourcingDefaultRepository(
            eventSourcingNetworkDataSource = mockEventSourcingNetworkDataSource,
            eventStore = mockEventStore,
            aggregateStore = mockAggregateStore,
            eventBus = mockEventBus,
            scope = CoroutineScope(Dispatchers.Default)
        )
    }

    @Test
    fun `aggregateState should flow data from eventStore`() = runTest {
        val eventsInStore = listOf(TestData.event1FromGroup1)

        everySuspend {
            mockEventStore.getEvents(any())
        } returns eventsInStore

        val aggregate1 = reduce(TestData.event1FromGroup1)

        sut.aggregateState(
            groupId = TestData.group1.id,
            aggregationKey = ShoppingListAggregate::class.qualifiedName!!,
            type = ShoppingListEvent::class,
            initial = ShoppingListAggregate.empty,
            isEmpty = { it.items.isEmpty() },
            serializer = ShoppingListAggregate.serializer()
        ).test {
            assertEquals(RepositoryState.Data(aggregate1), awaitItem())
        }
    }

    @Test
    fun `aggregateState should load new data from network by collecting Update with new latestEventId`() =
        runTest {
            val eventsInStore = listOf(TestData.event1FromGroup1)
            val newEventsFromNetwork = listOf(TestData.event2FromGroup1)

            everySuspend {
                mockEventStore.getEvents(any())
            } returns eventsInStore

            everySuspend {
                mockEventStore.getNextExpectedEventId(any())
            } returns 1

            everySuspend {
                mockEventSourcingNetworkDataSource.getEvents(any(), any())
            } returns LivingLinkResult.Success(
                GetEventSourcingEventsResponse(
                    events = newEventsFromNetwork
                )
            )

            val aggregate1 = reduce(TestData.event1FromGroup1)
            val aggregate2 = reduce(TestData.event1FromGroup1, TestData.event2FromGroup1)

            sut.aggregateState(
                groupId = TestData.group1.id,
                aggregationKey = ShoppingListAggregate::class.qualifiedName!!,
                type = ShoppingListEvent::class,
                initial = ShoppingListAggregate.empty,
                isEmpty = { it.items.isEmpty() },
                serializer = ShoppingListAggregate.serializer()
            ).test {
                eventBusEvents.emit(
                    EventBus.Event.GroupStateUpdated(
                        groupId = TestData.group1.id,
                        latestEventId = TestData.event1FromGroup1.eventId + 1
                    )
                )

                assertEquals(RepositoryState.Data(aggregate1), awaitItem())
                assertEquals(RepositoryState.Data(aggregate2), awaitItem())
            }
        }

    @Test
    fun `aggregateState should use aggregateStore if aggregate already exists`() = runTest {
        val cachedAggregate = reduce(TestData.event1FromGroup1)

        everySuspend {
            mockAggregateStore.get<ShoppingListAggregate>(any(), any())
        } returns cachedAggregate

        sut.aggregateState(
            groupId = TestData.group1.id,
            aggregationKey = ShoppingListAggregate::class.qualifiedName!!,
            type = ShoppingListEvent::class,
            initial = ShoppingListAggregate.empty,
            isEmpty = { it.items.isEmpty() },
            serializer = ShoppingListAggregate.serializer()
        ).test {
            assertEquals(RepositoryState.Data(cachedAggregate), awaitItem())
        }
    }

    @Test
    fun `aggregateState should not store events when incoming eventId is ahead of expected`() =
        runTest {
            val eventsInStore = listOf(TestData.event1FromGroup1)
            val newEventsFromNetwork = listOf(TestData.event3FromGroup1)

            everySuspend {
                mockEventStore.getEvents(any())
            } returns eventsInStore

            everySuspend {
                mockEventStore.getNextExpectedEventId(any())
            } returns 1

            everySuspend {
                mockEventSourcingNetworkDataSource.getEvents(any(), any())
            } returns LivingLinkResult.Success(
                GetEventSourcingEventsResponse(
                    events = newEventsFromNetwork
                )
            )

            val aggregate1 = reduce(TestData.event1FromGroup1)

            sut.aggregateState(
                groupId = TestData.group1.id,
                aggregationKey = ShoppingListAggregate::class.qualifiedName!!,
                type = ShoppingListEvent::class,
                initial = ShoppingListAggregate.empty,
                isEmpty = { it.items.isEmpty() },
                serializer = ShoppingListAggregate.serializer()
            ).test {
                eventBusEvents.emit(
                    EventBus.Event.GroupStateUpdated(
                        groupId = TestData.group1.id,
                        latestEventId = TestData.event3FromGroup1.eventId
                    )
                )

                assertEquals(RepositoryState.Data(aggregate1), awaitItem())
            }

            verifySuspend(VerifyMode.exhaustive) {
                mockAggregateStore.get(
                    cacheKey = cacheKey,
                    serializer = ShoppingListAggregate.serializer()
                )

                mockEventStore.getEvents(TestData.group1.id)

                mockAggregateStore.store(
                    cacheKey = cacheKey,
                    serializer = ShoppingListAggregate.serializer(),
                    aggregate = aggregate1
                )

                mockEventStore.getNextExpectedEventId(TestData.group1.id)

                mockEventSourcingNetworkDataSource.getEvents(
                    groupId = TestData.group1.id,
                    sinceEventIdExclusive = 0
                )
            }
        }

    private fun reduce(vararg events: EventSourcingEvent) =
        events.fold(ShoppingListAggregate.empty, ShoppingListAggregate::applyEvent)
}