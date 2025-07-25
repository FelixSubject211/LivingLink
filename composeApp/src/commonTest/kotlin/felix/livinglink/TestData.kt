package felix.livinglink

import felix.livinglink.eventSourcing.EventSourcingEvent
import felix.livinglink.group.Group
import kotlinx.datetime.Clock

object TestData {
    val group1 = Group(
        id = "group1",
        name = "group1Name",
        groupMemberIdsToName = emptyMap(),
        createdAt = Clock.System.now()
    )

    val event1FromGroup1 = EventSourcingEvent(
        eventId = 0,
        userId = "userId",
        groupId = group1.id,
        createdAt = Clock.System.now(),
        payload = TestEvent(id = "id1")
    )

    val event2FromGroup1 = EventSourcingEvent(
        eventId = 1,
        userId = "userId",
        groupId = group1.id,
        createdAt = Clock.System.now(),
        payload = TestEvent(id = "id2")
    )

    val event3FromGroup1 = EventSourcingEvent(
        eventId = 2,
        userId = "userId",
        groupId = group1.id,
        createdAt = Clock.System.now(),
        payload = TestEvent(id = "id3")
    )
}