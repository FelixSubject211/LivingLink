package felix.projekt.livinglink.composeApp.ui.listGroups.view

import ListGroupsLocalizables
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import felix.projekt.livinglink.composeApp.ui.listGroups.viewModel.ListGroupsAction

@Composable
fun ListGroupsEmptyScreen(
    modifier: Modifier = Modifier,
    dispatch: (ListGroupsAction) -> Unit
) {
    Box(
        modifier = modifier.padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = ListGroupsLocalizables.EmptyStateText(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { dispatch(ListGroupsAction.AddGroupSubmitted) }
            ) {
                Text(
                    text = ListGroupsLocalizables.AddGroupButtonTitle(),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

