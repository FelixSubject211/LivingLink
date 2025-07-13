package felix.livinglink.ui.groups.settings

import GroupsSettingsScreenLocalizables
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import felix.livinglink.ui.common.GroupedSection

@Composable
fun GroupSettingsScreenContent(
    loadableData: GroupSettingsViewModel.LoadableData,
    viewModel: GroupSettingsViewModel
) {
    val data = viewModel.data.collectAsState().value
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            GroupedSection(
                title = GroupsSettingsScreenLocalizables.sectionMembersTitle(),
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                loadableData.group.groupMembersSortedByRoleAndName.forEach { member ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = member.name,
                            modifier = Modifier.weight(1f)
                        )

                        if (member.isAdmin) {
                            val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            Text(
                                text = GroupsSettingsScreenLocalizables.adminSuffix(),
                                fontSize = 12.sp,
                                color = labelColor,
                                modifier = Modifier
                                    .background(
                                        color = labelColor.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(percent = 50)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        val isCurrentUserAdmin =
                            loadableData.group.adminUserIds.contains(loadableData.currentUserId)
                        if (isCurrentUserAdmin && !member.isAdmin && member.id != loadableData.currentUserId) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = null,
                                modifier = Modifier
                                    .clickable { viewModel.openUserMenu(member.id) }
                            )
                            DropdownMenu(
                                expanded = data.userMenuExpandedFor == member.id,
                                onDismissRequest = viewModel::closeUserMenu
                            ) {
                                DropdownMenuItem(
                                    text = { Text(GroupsSettingsScreenLocalizables.makeAdmin()) },
                                    onClick = { viewModel.showMakeAdminDialog(member.id) }
                                )
                                DropdownMenuItem(
                                    text = { Text(GroupsSettingsScreenLocalizables.removeUser()) },
                                    onClick = { viewModel.showRemoveUserDialog(member.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
        item {
            val isAdmin = loadableData.group.adminUserIds.contains(loadableData.currentUserId)

            GroupedSection(
                title = GroupsSettingsScreenLocalizables.sectionGeneralGroupTitle(),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = viewModel::showLeaveGroupDialog) {
                        Text(GroupsSettingsScreenLocalizables.leaveGroup())
                    }

                    if (isAdmin) {
                        Button(onClick = viewModel::showDeleteGroupDialog) {
                            Text(GroupsSettingsScreenLocalizables.deleteGroup())
                        }
                    }
                }

                if (isAdmin) {
                    Button(onClick = viewModel::createInviteCode) {
                        Text(GroupsSettingsScreenLocalizables.createInvite())
                    }
                }
            }
        }
    }
}