package felix.livinglink.eventSourcing.repository

import kotlinx.serialization.Serializable

@Serializable
data class CacheKey(
    val groupId: String,
    val aggregationKey: String
)
