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
fun <LOADABLE_DATA, DATA, LOADABLE_ERROR, ERROR, REQUEST_ERROR> LoadableStatefulView(
    viewModel: LoadableStatefulViewModel<LOADABLE_DATA, DATA, LOADABLE_ERROR, ERROR, REQUEST_ERROR>,
    modifier: Modifier,
    emptyContent: @Composable () -> Unit = {},
    loadingContent: @Composable () -> Unit = {},
    content: @Composable (loadableData: LOADABLE_DATA, data: DATA) -> Unit,
) {
    val loadableData = viewModel.loadableData.collectAsState().value
    val dataState = viewModel.data.collectAsState().value
    val errorState = viewModel.error.collectAsState().value
    val loadingState = viewModel.loading.collectAsState().value

    Box(modifier = modifier) {
        Column {
            if (loadingState) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }

            when (loadableData) {
                is LoadableViewModelState.State.Empty -> {
                    emptyContent()
                }

                is LoadableViewModelState.State.Loading -> {
                    loadingContent()
                }

                is LoadableViewModelState.State.Data -> {
                    content(loadableData.data, dataState)
                }
            }
        }

        errorState?.toAlert(
            navigator = viewModel.navigator,
            onDismissRequest = viewModel::closeError
        )
    }
}