package felix.livinglink.ui.groups.settings

import GroupsSettingsScreenLocalizables
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import felix.livinglink.ui.common.GroupedSection

@Composable
fun GroupSettingsScreenContent(
    viewModel: GroupSettingsViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            GroupedSection(
                title = GroupsSettingsScreenLocalizables.sectionGeneralGroupTitle(),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = viewModel::createInviteCode) {
                        Text(GroupsSettingsScreenLocalizables.createInvite())
                    }

                    Button(onClick = viewModel::showDeleteGroupDialog) {
                        Text(GroupsSettingsScreenLocalizables.deleteGroup())
                    }
                }
            }
        }
    }
}