package felix.livinglink.ui.groups.detail

import GroupsDetailScreenLocalizables
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import felix.livinglink.ui.groups.settings.GroupSettingsScreen
import felix.livinglink.ui.groups.settings.GroupSettingsViewModel
import felix.livinglink.ui.shoppingList.list.ShoppingListListScreen
import felix.livinglink.ui.shoppingList.list.ShoppingListListViewModel
import felix.livinglink.ui.taskBoard.list.TaskBoardListScreen
import felix.livinglink.ui.taskBoard.list.TaskBoardListViewModel

@Composable
fun GroupScreen(
    shoppingListListViewModel: ShoppingListListViewModel,
    taskBoardListViewModel: TaskBoardListViewModel,
    groupSettingsViewModel: GroupSettingsViewModel,
) {
    var selectedTab by remember { mutableStateOf(GroupDetailTab.SHOPPING_LIST) }

    Column(modifier = Modifier.fillMaxSize()) {

        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                GroupDetailTab.SHOPPING_LIST -> {
                    ShoppingListListScreen(shoppingListListViewModel)
                }

                GroupDetailTab.TASK_BOARD -> {
                    TaskBoardListScreen(taskBoardListViewModel)
                }

                GroupDetailTab.GROUP_SETTINGS -> {
                    GroupSettingsScreen(groupSettingsViewModel)
                }
            }
        }

        GroupDetailTabs(
            selected = selectedTab,
            onTabSelected = { selectedTab = it }
        )
    }
}

@Composable
private fun GroupDetailTabs(
    selected: GroupDetailTab,
    onTabSelected: (GroupDetailTab) -> Unit
) {
    val tabs = GroupDetailTab.entries.toTypedArray()

    TabRow(
        selectedTabIndex = tabs.indexOf(selected),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        tabs.forEach { tab ->
            Tab(
                selected = tab == selected,
                onClick = { onTabSelected(tab) },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = tab.icon(), contentDescription = null)
                        Text(tab.label())
                    }
                }
            )
        }
    }
}

private enum class GroupDetailTab {
    SHOPPING_LIST,
    TASK_BOARD,
    GROUP_SETTINGS;

    fun label() = when (this) {
        SHOPPING_LIST -> GroupsDetailScreenLocalizables.tabShoppingList()
        TASK_BOARD -> GroupsDetailScreenLocalizables.tabTaskBoard()
        GROUP_SETTINGS -> GroupsDetailScreenLocalizables.tapGroupSettings()
    }

    fun icon(): ImageVector = when (this) {
        SHOPPING_LIST -> Icons.AutoMirrored.Filled.List
        TASK_BOARD -> Icons.Filled.Home
        GROUP_SETTINGS -> Icons.Filled.Settings
    }
}