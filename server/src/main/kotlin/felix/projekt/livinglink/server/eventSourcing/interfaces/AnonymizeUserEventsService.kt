package felix.projekt.livinglink.server.eventSourcing.interfaces

interface AnonymizeUserEventsService {
    suspend operator fun invoke(groupId: String, userId: String)
}