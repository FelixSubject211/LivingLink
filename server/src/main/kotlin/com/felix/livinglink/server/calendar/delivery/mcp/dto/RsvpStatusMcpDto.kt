package com.felix.livinglink.server.calendar.delivery.mcp.dto

import com.felix.livinglink.server.calendar.domain.Participant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class RsvpStatusMcpDto {
    @SerialName("pending")
    Pending,

    @SerialName("accepted")
    Accepted,

    @SerialName("declined")
    Declined,

    @SerialName("maybe")
    Maybe,
    ;

    companion object {
        fun fromDomain(status: Participant.RsvpStatus): RsvpStatusMcpDto =
            when (status) {
                Participant.RsvpStatus.Pending -> Pending
                Participant.RsvpStatus.Accepted -> Accepted
                Participant.RsvpStatus.Declined -> Declined
                Participant.RsvpStatus.Maybe -> Maybe
            }
    }
}
