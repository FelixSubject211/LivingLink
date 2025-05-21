package felix.livinglink.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import felix.livinglink.ui.common.navigation.Navigator

@Composable
expect fun BackAwareScaffold(
    navigator: Navigator,
    title: String,
    actions: @Composable () -> Unit = {},
    content: @Composable (Modifier) -> Unit
)