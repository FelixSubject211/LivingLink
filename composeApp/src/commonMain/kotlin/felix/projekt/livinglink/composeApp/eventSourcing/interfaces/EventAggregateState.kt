package felix.projekt.livinglink.composeApp.eventSourcing.interfaces

sealed class EventAggregateState<out STATE> {
    data class Loading(val progress: Float) : EventAggregateState<Nothing>()
    data class Data<STATE>(
        val state: STATE,
        val lastEventId: Long
    ) : EventAggregateState<STATE>()
}