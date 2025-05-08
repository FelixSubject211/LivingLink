package felix.livinglink.ui.group

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun GroupScreenContent(
    loadableData: GroupViewModel.LoadableData,
    viewModel: GroupViewModel
) {
    Text(loadableData.group.toString())
}