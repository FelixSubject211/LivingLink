package felix.projekt.livinglink.server.eventSourcing.interfaces

interface DeleteEventsService {
    suspend operator fun invoke(groupId: String)
}