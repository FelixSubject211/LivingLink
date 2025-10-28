package felix.projekt.livinglink.composeApp.ui.group.view

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import felix.projekt.livinglink.composeApp.ui.core.view.BackNavigationIcon
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.ViewModel
import felix.projekt.livinglink.composeApp.ui.group.viewModel.GroupAction
import felix.projekt.livinglink.composeApp.ui.group.viewModel.GroupSideEffect
import felix.projekt.livinglink.composeApp.ui.group.viewModel.GroupState
import livinglink.composeapp.generated.resources.Res
import livinglink.composeapp.generated.resources.arrow_back_36px

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupScreen(
    viewModel: ViewModel<GroupState, GroupAction, GroupSideEffect>,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is GroupSideEffect.NavigateBack -> {
                    onNavigateBack()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.groupName ?: "") },
                navigationIcon = {
                    BackNavigationIcon(
                        drawableRes = Res.drawable.arrow_back_36px,
                        viewModel = viewModel,
                        onClickAction = GroupAction.NavigateBack
                    )
                }
            )
        }
    ) { innerPadding ->
        Text(
            state.toString(),
            modifier = Modifier.padding(innerPadding)
        )
    }
}