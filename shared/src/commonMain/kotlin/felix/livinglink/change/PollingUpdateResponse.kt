package felix.livinglink.change

import kotlinx.serialization.Serializable

@Serializable
data class PollingUpdateResponse(
    val changeId: String?,
    val nextPollInSeconds: Int
)