package felix.livinglink.ui.taskBoard.list

import TaskBoardListScreenLocalizables
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import felix.livinglink.ui.common.ModalBottomSheet

@Composable
fun TaskBoardListAddTaskSheet(
    data: TaskBoardListViewModel.Data,
    viewModel: TaskBoardListViewModel
) {
    ModalBottomSheet(
        title = TaskBoardListScreenLocalizables.addTaskSheetTitle(),
        confirmButtonText = TaskBoardListScreenLocalizables.addTaskSheetConfirm(),
        dismissButtonText = TaskBoardListScreenLocalizables.addTaskSheetCancel(),
        onDismiss = viewModel::closeAddTask,
        onConfirm = viewModel::addTask,
        confirmButtonEnabled = viewModel.addTaskConfirmButtonEnabled()
    ) {
        OutlinedTextField(
            value = data.addTaskTitle,
            onValueChange = viewModel::updateAddTaskTitle,
            label = { Text(TaskBoardListScreenLocalizables.addTaskSheetTitleLabel()) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = data.addTaskDescription,
            onValueChange = viewModel::updateAddTaskDescription,
            label = { Text(TaskBoardListScreenLocalizables.addTaskSheetDescriptionLabel()) },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )
    }
}