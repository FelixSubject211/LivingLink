package felix.livinglink.ui.common

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import felix.livinglink.ui.common.navigation.Navigator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun BackAwareScaffold(
    navigator: Navigator,
    title: String,
    actions: @Composable () -> Unit,
    content: @Composable (Modifier) -> Unit
) {
    if (navigator.canNavigateBack()) {
        BackHandler(onBack = navigator::pop)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = { actions() }
            )
        },
        content = { innerPadding -> content(Modifier.padding(innerPadding)) }
    )
}
