package felix.projekt.livinglink.composeApp.eventSourcing.application

import app.cash.turbine.turbineScope
import dev.mokkery.MockMode
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.answering.returnsBy
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.exhaustiveOrder
import dev.mokkery.verifyNoMoreCalls
import dev.mokkery.verifySuspend
import felix.projekt.livinglink.composeApp.auth.interfaces.GetAuthStateService
import felix.projekt.livinglink.composeApp.core.domain.Result
import felix.projekt.livinglink.composeApp.eventSourcing.domain.EventSourcingNetworkDataSource
import felix.projekt.livinglink.composeApp.eventSourcing.domain.EventStore
import felix.projekt.livinglink.composeApp.eventSourcing.domain.PollEventsResponse
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.Aggregator
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.EventAggregateState
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.EventSourcingEvent
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.EventTopic
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.TopicSubscription
import felix.projekt.livinglink.shared.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.jvm.JvmInline
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class EventSourcingDefaultRepositoryTest {
    private lateinit var mockEventStore: EventStore
    private lateinit var mockEventSourcingNetworkDataSource: EventSourcingNetworkDataSource
    private lateinit var mockGetAuthStateService: GetAuthStateService
    private lateinit var eventSynchronizer: EventSynchronizer
    private lateinit var sut: EventSourcingDefaultRepository
    private lateinit var scope: CoroutineScope

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun setup() {
        scope = CoroutineScope(UnconfinedTestDispatcher())
        mockEventStore = mock(mode = MockMode.autofill)
        mockEventSourcingNetworkDataSource = mock(mode = MockMode.autofill)
        mockGetAuthStateService = mock(mode = MockMode.autofill)
        eventSynchronizer = EventSynchronizer(
            eventStore = mockEventStore,
            eventSourcingNetworkDataSource = mockEventSourcingNetworkDataSource,
            scope = scope
        )

        every {
            mockGetAuthStateService.invoke()
        } returns flowOf(GetAuthStateService.AuthState.LoggedIn)

        sut = EventSourcingDefaultRepository(
            eventSynchronizer = eventSynchronizer,
            eventStore = mockEventStore,
            getAuthStateService = mockGetAuthStateService,
            scope = scope
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Two Aggregators on same subscription receive all events`() = runTest {
        val subscription = TopicSubscription(
            groupId = "g1",
            topic = TestTopic()
        )

        val aggA = testAggregator(
            groupId = "g1",
            suffix = "A"
        )

        val aggB = testAggregator(
            groupId = "g1",
            suffix = "B"
        )

        val events = mutableListOf<EventSourcingEvent>()

        everySuspend {
            mockEventStore.lastEventId(subscription = subscription)
        } returnsBy { events.lastOrNull()?.eventId ?: 0 }

        everySuspend {
            mockEventStore.append(
                subscription = subscription,
                events = any()
            )
        } calls { (_: TopicSubscription<*>, newEvents: List<EventSourcingEvent>) ->
            events += newEvents
        }

        everySuspend {
            mockEventStore.eventsSince(
                subscription = subscription,
                eventId = any()
            )
        } returnsBy { events }

        val event1 = EventSourcingEvent(
            eventId = 1,
            groupId = "g1",
            topic = "TEST",
            createdBy = "u",
            createdAtEpochMillis = 0,
            payload = json.encodeToJsonElement<TestEvent>(TestEvent.A)
        )

        val event2 = EventSourcingEvent(
            eventId = 2,
            groupId = "g1",
            topic = "TEST",
            createdBy = "u",
            createdAtEpochMillis = 0,
            payload = json.encodeToJsonElement<TestEvent>(TestEvent.B)
        )

        everySuspend {
            mockEventSourcingNetworkDataSource.pollEvents(
                groupId = "g1",
                topic = "TEST",
                lastKnownEventId = 0
            )
        } returns Result.Success(
            PollEventsResponse.Success(
                events = listOf(event1),
                totalEvents = 1,
                nextPollAfterMillis = 0
            )
        )

        everySuspend {
            mockEventSourcingNetworkDataSource.pollEvents(
                groupId = "g1",
                topic = "TEST",
                lastKnownEventId = 1
            )
        } returns Result.Success(
            PollEventsResponse.Success(
                events = listOf(event2),
                totalEvents = 2,
                nextPollAfterMillis = Long.MAX_VALUE
            )
        )

        turbineScope {
            val flowA = sut.getAggregate(aggregator = aggA)
            val flowB = sut.getAggregate(aggregator = aggB)

            val testFlowA = flowA.testIn(backgroundScope)
            val testFlowB = flowB.testIn(backgroundScope)

            val result1FlowA = (testFlowA.awaitItem())
            val result1FlowB = (testFlowB.awaitItem())

            assertIs<EventAggregateState.Data<*>>(result1FlowA)
            assertIs<EventAggregateState.Data<*>>(result1FlowB)

            assertEquals(listOf("A", "B"), result1FlowA.state)
            assertEquals(listOf("A", "B"), result1FlowB.state)
        }

        verifySuspend(exhaustiveOrder) {
            mockEventStore.eventsSince(subscription = subscription, eventId = 0)
            mockEventStore.lastEventId(subscription = subscription)
            mockEventSourcingNetworkDataSource.pollEvents(
                groupId = subscription.groupId,
                topic = subscription.topic.value,
                lastKnownEventId = 0
            )
            mockEventStore.lastEventId(subscription = subscription)
            mockEventStore.append(subscription = subscription, events = listOf(event1))
            mockEventStore.lastEventId(subscription = subscription)
            mockEventSourcingNetworkDataSource.pollEvents(
                groupId = subscription.groupId,
                topic = subscription.topic.value,
                lastKnownEventId = 1
            )
            mockEventStore.lastEventId(subscription = subscription)
            mockEventStore.append(subscription = subscription, events = listOf(event2))
            mockEventStore.eventsSince(subscription = subscription, eventId = 0)
        }

        verifyNoMoreCalls(mockEventStore, mockEventSourcingNetworkDataSource)
    }
}

@JvmInline
private value class TestTopic(override val value: String = "TEST") : EventTopic

@Serializable
private sealed interface TestEvent {
    @Serializable
    data object A : TestEvent

    @Serializable
    data object B : TestEvent
}

private fun testAggregator(
    groupId: String,
    suffix: String
): Aggregator<TestTopic, List<String>> {
    return object : Aggregator<TestTopic, List<String>> {
        override val id: String = "agg-$groupId-$suffix"

        override val subscription: TopicSubscription<TestTopic> = TopicSubscription(
            groupId = groupId,
            topic = TestTopic()
        )

        override val initialState: List<String> = emptyList()

        override fun apply(
            currentState: List<String>,
            events: List<EventSourcingEvent>
        ): List<String> {
            var state = currentState
            events.forEach { event ->
                val decoded = json.decodeFromJsonElement<TestEvent>(event.payload)
                state = state + decoded.toString()
            }
            return state
        }
    }
}