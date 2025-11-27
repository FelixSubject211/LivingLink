package felix.projekt.livinglink.composeApp.ui.shoppingListItemDetail.view

import ShoppingListItemDetailLocalizables
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import felix.projekt.livinglink.composeApp.ui.core.format.formatDateTime
import felix.projekt.livinglink.composeApp.ui.core.view.TextStack
import felix.projekt.livinglink.composeApp.ui.shoppingListItemDetail.viewModel.ShoppingListItemDetailState
import livinglink.composeapp.generated.resources.Res
import livinglink.composeapp.generated.resources.add_box_36px
import livinglink.composeapp.generated.resources.check_box_36px
import livinglink.composeapp.generated.resources.delete_36px
import livinglink.composeapp.generated.resources.indeterminate_check_box_36px
import org.jetbrains.compose.resources.painterResource
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun ShoppingListItemDetailEventItem(action: ShoppingListItemDetailState.Action) {
    val localizedActionType = when (action.actionType) {
        ShoppingListItemDetailState.ActionType.Created -> {
            ShoppingListItemDetailLocalizables.CreatedAction()
        }

        ShoppingListItemDetailState.ActionType.Checked -> {
            ShoppingListItemDetailLocalizables.CheckedAction()
        }

        ShoppingListItemDetailState.ActionType.Unchecked -> {
            ShoppingListItemDetailLocalizables.UncheckedAction()
        }

        ShoppingListItemDetailState.ActionType.Deleted -> {
            ShoppingListItemDetailLocalizables.DeletedAction()
        }
    }

    val icon = when (action.actionType) {
        ShoppingListItemDetailState.ActionType.Created -> {
            Res.drawable.add_box_36px
        }

        ShoppingListItemDetailState.ActionType.Checked -> {
            Res.drawable.check_box_36px
        }

        ShoppingListItemDetailState.ActionType.Unchecked -> {
            Res.drawable.indeterminate_check_box_36px
        }

        ShoppingListItemDetailState.ActionType.Deleted -> {
            Res.drawable.delete_36px
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = localizedActionType,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 8.dp)
            )

            TextStack(
                items = listOf(
                    action.userName ?: ShoppingListItemDetailLocalizables.UnknownUser(),
                    localizedActionType,
                    formatDateTime(action.createdAt)
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}