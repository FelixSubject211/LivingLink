package felix.projekt.livinglink.composeApp

import ListGroupsLocalizables
import SettingsLocalizables
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Cog
import compose.icons.fontawesomeicons.solid.Users
import felix.projekt.livinglink.composeApp.ui.listGroups.view.ListGroupsScreen
import felix.projekt.livinglink.composeApp.ui.settings.view.SettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabBar() {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val tabItems = listOf(
        ListGroupsLocalizables.Title(),
        SettingsLocalizables.Title()
    )

    val tabIcons = listOf(
        FontAwesomeIcons.Solid.Users,
        FontAwesomeIcons.Solid.Cog
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTabIndex) {
                0 -> ListGroupsScreen(AppModule.listGroupsViewModel)
                1 -> SettingsScreen(AppModule.settingsViewModel)
            }
        }

        PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
            tabItems.forEachIndexed { index, item ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(item) },
                    icon = {
                        Icon(
                            imageVector = tabIcons[index],
                            contentDescription = item,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                )
            }
        }
    }
}