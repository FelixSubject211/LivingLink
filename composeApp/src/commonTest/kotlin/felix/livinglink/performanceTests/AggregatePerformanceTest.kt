package felix.livinglink.performanceTests

import felix.livinglink.eventSourcing.EventSourcingEvent
import felix.livinglink.shoppingList.ShoppingListAggregate
import felix.livinglink.shoppingList.ShoppingListEvent
import felix.livinglink.shoppingList.ShoppingListItemHistoryAggregate
import felix.livinglink.shoppingList.ShoppingListSuggestionAggregate
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.time.measureTime

class AggregatePerformanceTest {

    private val eventCounts = listOf(10_000, 100_000, 1_000_000)

    data class TestCase<out A, out E : EventSourcingEvent.Payload>(
        val totalEvents: Int,
        val createInitial: () -> A,
        val generateEvents: () -> List<EventSourcingEvent<@UnsafeVariance E>>,
        val applyEvents: (@UnsafeVariance A, List<EventSourcingEvent<@UnsafeVariance E>>) -> A
    )

    @Test
    fun runAllPerformanceTests() {
        val testGroups: List<Pair<String, List<TestCase<*, *>>>> = listOf(
            "ShoppingListAggregate" to eventCounts.map { totalEvents ->
                val itemCount = (totalEvents + 2) / 3
                TestCase(
                    totalEvents = totalEvents,
                    createInitial = { ShoppingListAggregate.empty },
                    generateEvents = { generateMultiItemEvents(itemCount) },
                    applyEvents = ShoppingListAggregate::applyEvents
                )
            },
            "ShoppingListSuggestionAggregate" to eventCounts.map { totalEvents ->
                val itemCount = (totalEvents + 2) / 3
                TestCase(
                    totalEvents = totalEvents,
                    createInitial = { ShoppingListSuggestionAggregate.empty },
                    generateEvents = { generateMultiItemEvents(itemCount) },
                    applyEvents = ShoppingListSuggestionAggregate::applyEvents
                )
            },
            "ShoppingListItemHistoryAggregate" to eventCounts.map { totalEvents ->
                val toggleCount = totalEvents / 2
                val itemId = "item-1"
                TestCase(
                    totalEvents = totalEvents,
                    createInitial = { ShoppingListItemHistoryAggregate.empty(itemId) },
                    generateEvents = { generateSingleItemHistoryEvents(itemId, toggleCount) },
                    applyEvents = ShoppingListItemHistoryAggregate::applyEvents
                )
            }
        )

        for ((groupName, cases) in testGroups) {
            println(groupName)
            for (testCase in cases) {
                runTestCase(testCase)
            }
            println()
        }
    }

    private fun <A, E : EventSourcingEvent.Payload> runTestCase(testCase: TestCase<A, E>) {
        val events = testCase.generateEvents()
        val duration = measureTime {
            testCase.applyEvents(testCase.createInitial(), events.take(testCase.totalEvents))
        }
        println("  ${testCase.totalEvents} events: ${duration.inWholeMilliseconds} ms")
    }

    private fun generateMultiItemEvents(itemCount: Int): List<EventSourcingEvent<ShoppingListEvent>> {
        val now = Clock.System.now()
        var eventId = 1L
        return buildList(itemCount * 3) {
            for (i in 1..itemCount) {
                val itemId = "item-$i"
                add(
                    EventSourcingEvent(
                        eventId = eventId++,
                        userId = "user-1",
                        groupId = "group-1",
                        createdAt = now,
                        payload = ShoppingListEvent.ItemAdded(itemId = itemId, itemName = "Item $i")
                    )
                )
                add(
                    EventSourcingEvent(
                        eventId = eventId++,
                        userId = "user-1",
                        groupId = "group-1",
                        createdAt = now,
                        payload = ShoppingListEvent.ItemCompleted(itemId)
                    )
                )
                add(
                    EventSourcingEvent(
                        eventId = eventId++,
                        userId = "user-1",
                        groupId = "group-1",
                        createdAt = now,
                        payload = ShoppingListEvent.ItemUncompleted(itemId)
                    )
                )
            }
        }
    }

    private fun generateSingleItemHistoryEvents(
        itemId: String,
        toggleCount: Int
    ): List<EventSourcingEvent<ShoppingListEvent>> {
        val now = Clock.System.now()
        var eventId = 1L
        return buildList(1 + toggleCount * 2) {
            add(
                EventSourcingEvent(
                    eventId = eventId++,
                    userId = "user-1",
                    groupId = "group-1",
                    createdAt = now,
                    payload = ShoppingListEvent.ItemAdded(itemId = itemId, itemName = "HistoryItem")
                )
            )
            repeat(toggleCount) {
                add(
                    EventSourcingEvent(
                        eventId = eventId++,
                        userId = "user-1",
                        groupId = "group-1",
                        createdAt = now,
                        payload = ShoppingListEvent.ItemCompleted(itemId)
                    )
                )
                add(
                    EventSourcingEvent(
                        eventId = eventId++,
                        userId = "user-1",
                        groupId = "group-1",
                        createdAt = now,
                        payload = ShoppingListEvent.ItemUncompleted(itemId)
                    )
                )
            }
        }
    }
}