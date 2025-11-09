package felix.projekt.livinglink.composeApp.ui.listGroups.view

import ListGroupsLocalizables
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import felix.projekt.livinglink.composeApp.ui.listGroups.viewModel.ListGroupsAction
import felix.projekt.livinglink.composeApp.ui.listGroups.viewModel.ListGroupsState
import livinglink.composeapp.generated.resources.Res
import livinglink.composeapp.generated.resources.add_36px
import livinglink.composeapp.generated.resources.close_36px
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ListGroupsFabMenu(
    state: ListGroupsState,
    dispatch: (ListGroupsAction) -> Unit
) {
    FloatingActionButtonMenu(
        expanded = state.menuExpanded,
        button = {
            ToggleFloatingActionButton(
                checked = state.menuExpanded,
                onCheckedChange = {
                    dispatch(
                        when (it) {
                            true -> ListGroupsAction.ExpandMenu
                            false -> ListGroupsAction.CloseMenu
                        }
                    )
                }
            ) {
                val icon = if (checkedProgress < 0.5f) {
                    painterResource(Res.drawable.add_36px)
                } else {
                    painterResource(Res.drawable.close_36px)
                }

                Icon(
                    painter = icon,
                    contentDescription = null,
                    modifier = Modifier.animateIcon(::checkedProgress)
                )
            }
        },
        horizontalAlignment = Alignment.End
    ) {
        FloatingActionButtonMenuItem(
            onClick = { dispatch(ListGroupsAction.AddGroupSubmitted) },
            text = { Text(ListGroupsLocalizables.AddGroupButtonTitle()) },
            icon = {}
        )
    }
}