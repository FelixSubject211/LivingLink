package felix.projekt.livinglink.server.eventSourcing.interfaces

import felix.projekt.livinglink.server.eventSourcing.domain.PollEventsResult

fun interface PollEventsUseCase {
    suspend operator fun invoke(
        userId: String,
        groupId: String,
        topic: String,
        lastKnownEventId: Long
    ): PollEventsResult
}
