package felix.projekt.livinglink.composeApp.ui.listGroups.view

import ListGroupsLocalizables
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import felix.projekt.livinglink.composeApp.ui.core.view.ListItem
import felix.projekt.livinglink.composeApp.ui.core.view.TextStack
import felix.projekt.livinglink.composeApp.ui.core.view.colorFromName
import felix.projekt.livinglink.composeApp.ui.listGroups.viewModel.ListGroupsGroup

@Composable
fun ListGroupsItem(group: ListGroupsGroup, modifier: Modifier) {
    ListItem(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(colorFromName(group.name)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = group.name.firstOrNull()?.uppercase() ?: "?",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.width(16.dp))
            TextStack(
                items = listOf(
                    group.name,
                    ListGroupsLocalizables.GroupMemberCount(group.memberCount)
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}