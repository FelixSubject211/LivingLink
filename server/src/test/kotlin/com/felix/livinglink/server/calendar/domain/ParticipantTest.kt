package com.felix.livinglink.server.calendar.domain

import com.felix.livinglink.server.calendar.domain.Participant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

class ParticipantTest {
    private val t0 = Instant.fromEpochSeconds(1_700_000_000)

    @Test
    fun `currentStatus is Pending when there are no rsvp events`() {
        val participant = Participant(userId = "user-a", rsvpEvents = emptyList())

        assertEquals(Participant.RsvpStatus.Pending, participant.currentStatus)
    }

    @Test
    fun `currentStatus follows the last rsvp event`() {
        val participant =
            Participant(
                userId = "user-a",
                rsvpEvents =
                    listOf(
                        Participant.RsvpEvent(status = Participant.RsvpStatus.Accepted, at = t0),
                        Participant.RsvpEvent(
                            status = Participant.RsvpStatus.Maybe,
                            at = t0 + 1.hours,
                        ),
                        Participant.RsvpEvent(
                            status = Participant.RsvpStatus.Declined,
                            at = t0 + 2.hours,
                        ),
                    ),
            )

        assertEquals(Participant.RsvpStatus.Declined, participant.currentStatus)
    }
}
