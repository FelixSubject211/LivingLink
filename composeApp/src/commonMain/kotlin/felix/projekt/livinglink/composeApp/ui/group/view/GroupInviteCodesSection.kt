package felix.projekt.livinglink.composeApp.ui.group.view

import GroupLocalizables
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import felix.projekt.livinglink.composeApp.ui.core.view.LoadableText
import felix.projekt.livinglink.composeApp.ui.core.view.TextStack
import felix.projekt.livinglink.composeApp.ui.group.viewModel.GroupAction
import felix.projekt.livinglink.composeApp.ui.group.viewModel.GroupState
import livinglink.composeapp.generated.resources.Res
import livinglink.composeapp.generated.resources.delete_36px
import org.jetbrains.compose.resources.painterResource

@Composable
fun GroupInviteCodesSection(
    state: GroupState,
    dispatch: (GroupAction) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(bottom = 4.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = GroupLocalizables.InviteCodesSectionTitle(),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.alignByBaseline()
        )

        TextButton(
            onClick = { dispatch(GroupAction.StartInviteCodeCreation) },
            modifier = Modifier.alignByBaseline()
        ) {
            Text(GroupLocalizables.CreateInviteCodeButtonTitle())
        }
    }

    state.inviteCodes.forEach { inviteCode ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextStack(
                    items = listOf(
                        inviteCode.name,
                        state.memberIdToMemberName[inviteCode.creatorId]?.let {
                            GroupLocalizables.InviteCodeCreatedBy(it)
                        } ?: GroupLocalizables.InviteCodeCreatedByUnknownUser(),
                        GroupLocalizables.InviteCodeUsageCount(inviteCode.usages)
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                IconButton(
                    onClick = { dispatch(GroupAction.SubmitInviteCodeDeletion(inviteCode.id)) },
                    enabled = !state.deleteInviteCodeIsLoading
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.delete_36px),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    when (val creationState = state.inviteCodeCreation) {
        is GroupState.InviteCodeCreationState.Input -> {
            AlertDialog(
                onDismissRequest = { dispatch(GroupAction.InviteCodeCreationCancelled) },
                title = { Text(GroupLocalizables.CreatedInviteCodeDialogTitle()) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = creationState.name,
                            onValueChange = { dispatch(GroupAction.InviteCodeNameChanged(it)) },
                            label = { Text(GroupLocalizables.InviteCodeNameLabel()) },
                            singleLine = true
                        )

                        AnimatedVisibility(visible = creationState.error != null) {
                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = creationState.error?.localized().orEmpty(),
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { dispatch(GroupAction.SubmitInviteCodeCreation) },
                        enabled = !creationState.isLoading && creationState.name.isNotEmpty()
                    ) {
                        LoadableText(
                            text = GroupLocalizables.SubmitInviteCodeButtonTitle(),
                            isLoading = creationState.isLoading
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { dispatch(GroupAction.InviteCodeCreationCancelled) },
                        enabled = !creationState.isLoading
                    ) {
                        Text(GroupLocalizables.CancelInviteCodeButtonTitle())
                    }
                }
            )
        }

        is GroupState.InviteCodeCreationState.Success -> {
            AlertDialog(
                onDismissRequest = { dispatch(GroupAction.InviteCodeCreationCancelled) },
                title = { Text(GroupLocalizables.InviteCodeCreatedDialogTitle()) },
                text = {
                    Text(
                        text = creationState.key,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                confirmButton = {
                    TextButton(onClick = { dispatch(GroupAction.CopyInviteCode(creationState.key)) }) {
                        Text(GroupLocalizables.CopyInviteCodeButtonTitle())
                    }
                },
                dismissButton = {
                    TextButton(onClick = { dispatch(GroupAction.InviteCodeCreationCancelled) }) {
                        Text(GroupLocalizables.CloseInviteCodeDialogButtonTitle())
                    }
                }
            )
        }

        else -> {}
    }

    if (state.showDeleteInviteCodeConfirmation) {
        AlertDialog(
            onDismissRequest = { dispatch(GroupAction.CancelInviteCodeDeletion) },
            title = { Text(GroupLocalizables.DeleteInviteCodeDialogTitle()) },
            text = {
                Text(text = GroupLocalizables.DeleteInviteCodeDialogText())
            },
            confirmButton = {
                TextButton(
                    onClick = { dispatch(GroupAction.ConfirmInviteCodeDeletion) },
                    enabled = !state.deleteInviteCodeIsLoading
                ) {
                    LoadableText(
                        text = GroupLocalizables.DeleteInviteCodeDialogConfirm(),
                        isLoading = state.deleteInviteCodeIsLoading
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { dispatch(GroupAction.CancelInviteCodeDeletion) },
                    enabled = !state.deleteInviteCodeIsLoading
                ) {
                    Text(GroupLocalizables.DeleteInviteCodeDialogCancel())
                }
            }
        )
    }
}