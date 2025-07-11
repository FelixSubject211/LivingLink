package felix.livinglink.ui.taskBoard.list

import TaskBoardListScreenLocalizables
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import felix.livinglink.ui.common.BackAwareScaffold
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

    BackAwareScaffold(
        navigator = viewModel.navigator,
        title = TaskBoardListScreenLocalizables.navigationTitle(),
    ) { innerPadding ->
        LoadableStatefulView(
            viewModel = viewModel,
            modifier = innerPadding,
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
}