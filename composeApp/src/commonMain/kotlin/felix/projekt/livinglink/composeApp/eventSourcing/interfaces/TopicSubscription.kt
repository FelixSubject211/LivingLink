package felix.projekt.livinglink.composeApp.eventSourcing.interfaces

data class TopicSubscription<TTopic : EventTopic>(
    val groupId: String,
    val topic: TTopic
)