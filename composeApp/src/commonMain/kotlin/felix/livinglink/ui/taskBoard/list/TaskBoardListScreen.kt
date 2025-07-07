package felix.livinglink.ui.taskBoard.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import felix.livinglink.ui.common.state.LoadableStatefulView

@Composable
fun TaskBoardListScreen(viewModel: TaskBoardListViewModel) {
    val data = viewModel.data.collectAsState().value

    if (data.showAddTask) {
        TaskBoardListAddTaskSheet(
            data = data,
            viewModel = viewModel
        )
    }

    LoadableStatefulView(
        viewModel = viewModel,
        modifier = Modifier,
        emptyContent = {
            TaskBoardListEmptyContent(viewModel)
        },
        content = { loadableDate, _ ->
            TaskBoardListScreenContent(
                loadableData = loadableDate,
                viewModel = viewModel
            )
        }
    )
}