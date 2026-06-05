package com.felix.livinglink.server.calendar.domain

import com.felix.livinglink.server.calendar.domain.EventCategory
import com.felix.livinglink.server.calendar.domain.EventSpan
import com.felix.livinglink.server.calendar.domain.Participant
import com.felix.livinglink.server.calendar.domain.ScheduledEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class ScheduledEventTest {
    private val t0 = Instant.fromEpochSeconds(1_700_000_000)

    @Test
    fun `referencedUserIds contains only the creator when there are no participants`() {
        val event = scheduledEvent(createdByUserId = "creator", participants = emptyList())

        assertEquals(setOf("creator"), event.referencedUserIds)
    }

    @Test
    fun `referencedUserIds is the creator plus every participant, deduplicated`() {
        val event =
            scheduledEvent(
                createdByUserId = "creator",
                participants =
                    listOf(
                        Participant(userId = "creator", rsvpEvents = emptyList()),
                        Participant(userId = "user-a", rsvpEvents = emptyList()),
                        Participant(userId = "user-a", rsvpEvents = emptyList()),
                        Participant(userId = "user-b", rsvpEvents = emptyList()),
                    ),
            )

        assertEquals(setOf("creator", "user-a", "user-b"), event.referencedUserIds)
    }

    private fun scheduledEvent(
        createdByUserId: String = "creator",
        participants: List<Participant> = emptyList(),
    ): ScheduledEvent =
        ScheduledEvent(
            sourceEventId = "src-1",
            title = "event",
            description = null,
            createdByUserId = createdByUserId,
            span = EventSpan.Timed(start = t0, end = t0),
            participants = participants,
            category = EventCategory.None,
            createdAt = t0,
            updatedAt = t0,
        )
}
