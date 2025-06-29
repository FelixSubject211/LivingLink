package felix.livinglink.ui.shoppingListItem

import ShoppingListItemScreenLocalizables
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import felix.livinglink.eventSourcing.EventSourcingEvent
import felix.livinglink.shoppingList.ShoppingListEvent
import felix.livinglink.ui.common.DateFormatStyle
import felix.livinglink.ui.common.formatWith
import kotlinx.coroutines.flow.Flow

@Composable
fun ShoppingListItemEventHistoryItem(
    userNameFlow: Flow<String?>,
    event: EventSourcingEvent<ShoppingListEvent>
) {
    val userName by userNameFlow.collectAsState(initial = null)
    val displayName = userName ?: ShoppingListItemScreenLocalizables.eventPerformedByUnknown()

    val (descriptionText, icon) = when (event.payload) {
        is ShoppingListEvent.ItemAdded -> {
            ShoppingListItemScreenLocalizables.eventAdded(displayName) to Icons.Default.Add
        }

        is ShoppingListEvent.ItemCompleted -> {
            ShoppingListItemScreenLocalizables.eventCompleted(displayName) to Icons.Default.Check
        }

        is ShoppingListEvent.ItemUncompleted -> {
            ShoppingListItemScreenLocalizables.eventUncompleted(displayName) to Icons.Default.Close
        }

        else -> {
            ShoppingListItemScreenLocalizables.eventUnknown(displayName) to Icons.Default.Close
        }
    }

    val formattedDate = event.createdAt.formatWith(DateFormatStyle.DATE_TIME)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = descriptionText,
            modifier = Modifier.size(36.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Column {
            Text(
                text = descriptionText,
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
