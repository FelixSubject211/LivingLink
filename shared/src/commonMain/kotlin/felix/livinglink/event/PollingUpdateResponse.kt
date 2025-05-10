package felix.livinglink.event

import kotlinx.serialization.Serializable

@Serializable
data class PollingUpdateResponse(
    val membershipChangeId: String?,
    val latestEventId: Long?,
    val nextPollInSeconds: Int
)