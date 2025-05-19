package felix.livinglink.eventSourcing

import felix.livinglink.common.BaseIntegrationTest
import felix.livinglink.common.TestData
import felix.livinglink.common.addSampleGroups
import felix.livinglink.common.addSampleUsers
import felix.livinglink.common.get
import felix.livinglink.common.loginUser
import felix.livinglink.common.post
import felix.livinglink.module
import felix.livinglink.shoppingList.ShoppingListEvent
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFails

class EventSourcingRoutesTest : BaseIntegrationTest() {

    @Test
    fun `should append multiple events and return only newer events when filtered by eventId`() =
        testApplication {
            // Arrange
            application { module(config = config) }

            database.addSampleUsers(user = TestData.alice)
            database.addSampleGroups(group = TestData.groupOwnedByAlice1)

            val payload1 = ShoppingListEvent.ItemAdded(itemId = "itemId1", itemName = "itemName1")
            val payload2 = ShoppingListEvent.ItemAdded(itemId = "itemId2", itemName = "itemName2")

            val token = client.loginUser(
                username = TestData.alice.username,
                password = TestData.alice.password
            ).accessToken

            // Act
            val appendFirst: AppendEventSourcingEventResponse = client.post(
                urlString = "eventSourcing/append",
                request = AppendEventSourcingEventRequest(
                    groupId = TestData.groupOwnedByAlice1.id,
                    payload = payload1
                ),
                token = token
            )

            val allEvents: GetEventSourcingEventsResponse = client.get(
                urlString = "eventSourcing/events?groupId=${TestData.groupOwnedByAlice1.id}",
                token = token
            )

            val appendSecond: AppendEventSourcingEventResponse = client.post(
                urlString = "eventSourcing/append",
                request = AppendEventSourcingEventRequest(
                    groupId = TestData.groupOwnedByAlice1.id,
                    payload = payload2
                ),
                token = token
            )

            val eventsAfterSecond: GetEventSourcingEventsResponse = client.get(
                urlString = "eventSourcing/events?" +
                        "groupId=${TestData.groupOwnedByAlice1.id}" +
                        "&sinceEventIdExclusive=${appendSecond.event.eventId}",
                token = token
            )

            // Assert
            assertEquals(
                AppendEventSourcingEventResponse(
                    EventSourcingEvent(
                        eventId = 0,
                        userId = TestData.alice.id,
                        groupId = TestData.groupOwnedByAlice1.id,
                        createdAt = appendFirst.event.createdAt,
                        payload = payload1
                    )
                ),
                appendFirst
            )

            assertContentEquals(
                listOf(
                    EventSourcingEvent(
                        eventId = 0,
                        userId = TestData.alice.id,
                        groupId = TestData.groupOwnedByAlice1.id,
                        createdAt = appendFirst.event.createdAt,
                        payload = payload1
                    )
                ),
                allEvents.events
            )

            assertEquals(
                AppendEventSourcingEventResponse(
                    EventSourcingEvent(
                        eventId = 1,
                        userId = TestData.alice.id,
                        groupId = TestData.groupOwnedByAlice1.id,
                        createdAt = appendSecond.event.createdAt,
                        payload = payload2
                    )
                ),
                appendSecond
            )

            assertContentEquals(
                emptyList(),
                eventsAfterSecond.events
            )
        }

    @Test
    fun `should fail when user not in group tries to append event`() = testApplication {
        // Arrange
        application { module(config = config) }

        database.addSampleUsers(user = TestData.bob)
        database.addSampleGroups(group = TestData.groupOwnedByBob)

        val token = client.loginUser(TestData.bob.username, TestData.bob.password).accessToken

        // Act & Assert
        assertFails {
            client.post(
                urlString = "eventSourcing/append",
                request = AppendEventSourcingEventRequest(
                    groupId = TestData.groupOwnedByAlice1.id,
                    payload = ShoppingListEvent.ItemAdded(itemId = "itemId", itemName = "itemName")
                ),
                token = token
            )
        }
    }

    @Test
    fun `should fail when user not in group tries to get events`() = testApplication {
        // Arrange
        application { module(config = config) }

        database.addSampleUsers(user = TestData.bob)
        database.addSampleGroups(group = TestData.groupOwnedByBob)

        val token = client.loginUser(TestData.bob.username, TestData.bob.password).accessToken

        // Act & Assert
        assertFails {
            client.get<AppendEventSourcingEventResponse>(
                urlString = "eventSourcing/events?groupId=${TestData.groupOwnedByAlice1.id}",
                token = token
            )
        }
    }
}