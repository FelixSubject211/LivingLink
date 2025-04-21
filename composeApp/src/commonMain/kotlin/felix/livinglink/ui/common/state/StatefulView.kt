package felix.livinglink.ui.common.state

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import felix.livinglink.ui.common.toAlert

@Composable
fun <DATA, ERROR, REQUEST_ERROR> StatefulView(
    viewModel: StatefulViewModel<DATA, ERROR, REQUEST_ERROR>,
    modifier: Modifier,
    content: @Composable (data: DATA) -> Unit,
) {
    val dataState = viewModel.data.collectAsState()
    val errorState = viewModel.error.collectAsState()
    val loadingState = viewModel.loading.collectAsState()

    Box(modifier = modifier) {
        Column {
            if (loadingState.value) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }

            content(dataState.value)
        }

        errorState.value?.toAlert(
            navigator = viewModel.navigator,
            onDismissRequest = viewModel::closeError
        )
    }
}