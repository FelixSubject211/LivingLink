package felix.projekt.livinglink.composeApp.eventSourcing.interfaces

interface Aggregator<TTopic : EventTopic, TState> {
    val id: String
    val subscription: TopicSubscription<TTopic>
    val initialState: TState
    fun apply(currentState: TState, events: List<EventSourcingEvent>): TState
}