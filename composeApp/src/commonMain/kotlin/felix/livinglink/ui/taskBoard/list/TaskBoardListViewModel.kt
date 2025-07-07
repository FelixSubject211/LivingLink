package felix.livinglink.ui.taskBoard.list

import felix.livinglink.common.model.LivingLinkResult
import felix.livinglink.eventSourcing.repository.EventSourcingRepository
import felix.livinglink.taskBoard.TaskBoardAggregate
import felix.livinglink.taskBoard.TaskBoardEvent
import felix.livinglink.ui.common.navigation.Navigator
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class TaskBoardListViewModel(
    val groupId: String,
    override val navigator: Navigator,
    private val eventSourcingRepository: EventSourcingRepository,
    private val viewModelState: TaskBoardListViewModelState
) : ShoppingListListStatefulViewModel {
    override val loadableData = viewModelState.loadableData
    override val data = viewModelState.data
    override val error = viewModelState.error
    override val loading = viewModelState.loading
    override fun closeError() = viewModelState.closeError()
    override fun cancel() = viewModelState.cancel()

    fun showAddTask() = viewModelState.perform { data ->
        data.copy(showAddTask = true)
    }

    fun closeAddTask() = viewModelState.perform { data ->
        data.copy(
            showAddTask = false,
            addTaskTitle = "",
            addTaskDescription = ""
        )
    }

    @OptIn(ExperimentalUuidApi::class)
    fun addTask() = viewModelState.perform(
        request = { currentData ->
            eventSourcingRepository.addEvent(
                groupId = groupId,
                payload = TaskBoardEvent.TaskCreated(
                    taskId = Uuid.random().toString(),
                    title = currentData.addTaskTitle,
                    description = currentData.addTaskDescription
                )
            )
        },
        onSuccess = { currentData, _ ->
            LivingLinkResult.Success(
                currentData.copy(
                    showAddTask = false,
                    addTaskTitle = "",
                    addTaskDescription = ""
                )
            )
        }
    )

    fun addTaskConfirmButtonEnabled(): Boolean {
        return data.value.addTaskTitle.isNotBlank()
    }

    fun updateAddTaskTitle(title: String) = viewModelState.perform { current ->
        current.copy(addTaskTitle = title)
    }

    fun updateAddTaskDescription(description: String) = viewModelState.perform { current ->
        current.copy(addTaskDescription = description)
    }

    companion object {
        val initialState = Data(
            showAddTask = false,
            addTaskTitle = "",
            addTaskDescription = ""
        )
    }

    data class Data(
        val showAddTask: Boolean,
        val addTaskTitle: String,
        val addTaskDescription: String
    )

    data class LoadableData(
        val aggregate: TaskBoardAggregate
    )
}