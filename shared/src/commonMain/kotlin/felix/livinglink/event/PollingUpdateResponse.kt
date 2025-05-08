package felix.livinglink.event

import kotlinx.serialization.Serializable

@Serializable
data class PollingUpdateResponse(
    val changeId: String?,
    val nextPollInSeconds: Int
)