package felix.livinglink.taskBoard

import felix.livinglink.eventSourcing.EventSourcingEvent
import felix.livinglink.eventSourcing.repository.Aggregate
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

@Serializable
data class TaskBoardAggregate(
    private val tasks: LinkedHashMap<String, Task> = linkedMapOf()
) : Aggregate<TaskBoardAggregate, TaskBoardEvent> {

    @Serializable
    data class Task(
        val id: String,
        val title: String,
        val description: String
    )

    fun tasksReversed(): List<Task> = tasks.values.reversed()

    override fun applyEvent(event: EventSourcingEvent<TaskBoardEvent>): TaskBoardAggregate {
        val newTasks = LinkedHashMap(tasks)

        when (val payload = event.payload) {
            is TaskBoardEvent.TaskCreated -> {
                val task = Task(
                    id = payload.taskId,
                    title = payload.title,
                    description = payload.description
                )
                newTasks[payload.taskId] = task
            }
        }

        return copy(tasks = newTasks)
    }

    override fun isEmpty(): Boolean = tasks.isEmpty()

    override fun anonymizeUser(originalUserId: String): TaskBoardAggregate = this

    @OptIn(InternalSerializationApi::class)
    override fun serializer(): KSerializer<out TaskBoardAggregate> {
        return this::class.serializer()
    }

    companion object {
        val empty = TaskBoardAggregate()
    }
}