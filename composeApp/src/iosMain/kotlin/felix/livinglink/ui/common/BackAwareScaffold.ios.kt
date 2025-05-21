package felix.livinglink.ui.common

import CommonLocalizables
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                navigationIcon = {
                    if (navigator.canNavigateBack()) {
                        IconButton(onClick = navigator::pop) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = CommonLocalizables.ok()
                            )
                        }
                    }
                },
                actions = { actions() }
            )
        },
        content = { innerPadding -> content(Modifier.padding(innerPadding)) }
    )
}
