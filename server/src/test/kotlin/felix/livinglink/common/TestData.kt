package felix.livinglink.common

import felix.livinglink.auth.RefreshToken
import felix.livinglink.eventSourcing.EventSourcingEvent
import felix.livinglink.eventSourcing.EventSourcingStore
import felix.livinglink.group.Group
import felix.livinglink.json
import felix.livinglink.shoppingList.ShoppingListEvent
import kotlinx.datetime.Instant
import kotlinx.serialization.PolymorphicSerializer

object TestData {
    val fixedTime: Instant = Instant.parse("2020-01-01T00:00:00Z")

    val alice = RawUser(
        id = "alice-id",
        username = "alice",
        password = "password1"
    )

    val bob = RawUser(
        id = "bob-id",
        username = "bob",
        password = "password2"
    )

    val refreshTokenAlice = RefreshToken(
        token = "alice-refresh-token",
        userId = alice.id,
        username = alice.username,
        expiresAt = fixedTime.toEpochMilliseconds()
    )

    val groupOwnedByAlice1 = Group(
        id = "group-alice-1",
        name = "Study Group",
        groupMemberIdsToName = mapOf(alice.id to alice.username),
        createdAt = fixedTime
    )

    val groupOwnedByAlice2 = Group(
        id = "group-alice-2",
        name = "Project Team",
        groupMemberIdsToName = mapOf(alice.id to alice.username),
        createdAt = fixedTime
    )

    val groupOwnedByBob = Group(
        id = "group-bob-1",
        name = "Gaming Buddies",
        groupMemberIdsToName = mapOf(bob.id to bob.username),
        createdAt = fixedTime
    )

    val groupAliceAndBob = Group(
        id = "group-alice-bob-1",
        name = "Alice and Bob",
        groupMemberIdsToName = mapOf(alice.id to alice.username, bob.id to bob.username),
        createdAt = fixedTime
    )

    private val event1 = ShoppingListEvent.ItemAdded(itemId = "itemId", itemName = "itemName")
    private val event1Type = event1::class.qualifiedName!!
    private val event1PayloadJson = json.encodeToString(
        PolymorphicSerializer(EventSourcingEvent.Payload::class),
        event1
    )

    val eventsGroupFromAliceAndBob = listOf(
        EventSourcingStore.Event(
            groupId = groupAliceAndBob.id,
            eventId = 0L,
            eventType = event1Type,
            userId = alice.id,
            createdAt = fixedTime,
            payload = event1PayloadJson
        ),
        EventSourcingStore.Event(
            groupId = groupAliceAndBob.id,
            eventId = 1L,
            eventType = event1Type,
            userId = bob.id,
            createdAt = fixedTime,
            payload = event1PayloadJson
        ),
        EventSourcingStore.Event(
            groupId = groupAliceAndBob.id,
            eventId = 2L,
            eventType = event1Type,
            userId = alice.id,
            createdAt = fixedTime,
            payload = event1PayloadJson
        )
    )
}