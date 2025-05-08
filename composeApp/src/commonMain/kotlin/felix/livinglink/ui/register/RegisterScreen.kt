package felix.livinglink.ui.register

import RegisterScreenLocalizables
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import felix.livinglink.ui.common.state.StatefulView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(viewModel: RegisterViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(RegisterScreenLocalizables.navigationTitle()) },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { innerPadding ->
        StatefulView(
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding)
        ) { data ->
            RegisterScreenContent(
                data = data,
                viewModel = viewModel,
                modifier = Modifier.imePadding()
            )
        }
    }
}