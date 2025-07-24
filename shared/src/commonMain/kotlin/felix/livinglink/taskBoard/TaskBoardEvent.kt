package felix.livinglink.taskBoard

import felix.livinglink.eventSourcing.EventSourcingEvent
import kotlinx.serialization.Serializable

@Serializable
sealed class TaskBoardEvent : EventSourcingEvent.Payload {
    abstract val taskId: String

    @Serializable
    data class TaskCreated(
        override val taskId: String,
        val title: String,
        val description: String,
        val memberIds: List<String>
    ) : TaskBoardEvent()
}