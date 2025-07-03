package felix.livinglink.eventSourcing.repository

import kotlinx.serialization.Serializable

@Serializable
data class AggregateSnapshot<AGGREGATE>(
    val aggregate: AGGREGATE,
    val lastSeenGlobalEventId: Long
)