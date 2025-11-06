package felix.projekt.livinglink.composeApp.ui.group.view

import GroupLocalizables
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import felix.projekt.livinglink.composeApp.ui.core.view.BackNavigationIcon
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.ViewModel
import felix.projekt.livinglink.composeApp.ui.group.viewModel.GroupAction
import felix.projekt.livinglink.composeApp.ui.group.viewModel.GroupSideEffect
import felix.projekt.livinglink.composeApp.ui.group.viewModel.GroupState
import kotlinx.coroutines.flow.collectLatest
import livinglink.composeapp.generated.resources.Res
import livinglink.composeapp.generated.resources.arrow_back_36px
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun GroupScreen(
    viewModel: ViewModel<GroupState, GroupAction, GroupSideEffect>,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collectLatest { sideEffect ->
            when (sideEffect) {
                is GroupSideEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(sideEffect.localized())
                }

                is GroupSideEffect.CopyToClipboard.CopiedInviteCode -> {
                    clipboardManager.setText(
                        AnnotatedString(text = sideEffect.inviteCodeKey)
                    )
                }

                is GroupSideEffect.NavigateBack -> {
                    onNavigateBack()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(GroupLocalizables.Title(state.groupName ?: "")) },
                navigationIcon = {
                    BackNavigationIcon(
                        drawableRes = Res.drawable.arrow_back_36px,
                        viewModel = viewModel,
                        onClickAction = GroupAction.NavigateBack
                    )
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            GroupInviteCodesSection(
                state = state,
                dispatch = viewModel::dispatch
            )
        }
    }
}