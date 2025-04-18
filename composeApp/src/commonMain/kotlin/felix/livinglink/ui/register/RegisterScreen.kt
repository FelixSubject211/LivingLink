package felix.livinglink.ui.register

import RegisterScreenLocalizables
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
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
            RegisterContent(
                data = data,
                viewModel = viewModel,
                modifier = Modifier.imePadding()
            )
        }
    }
}

@Composable
private fun RegisterContent(
    data: RegisterViewModel.Data,
    viewModel: RegisterViewModel,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Column(
            verticalArrangement = Arrangement.spacedBy(56.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                label = { Text(RegisterScreenLocalizables.usernameLabel()) },
                value = data.username,
                onValueChange = viewModel::updateUsername,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                label = { Text(RegisterScreenLocalizables.passwordLabel()) },
                value = data.password,
                onValueChange = viewModel::updatePassword,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                label = { Text(RegisterScreenLocalizables.confirmPasswordLabel()) },
                value = data.confirmPassword,
                onValueChange = viewModel::updateConfirmPassword,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { viewModel.register() }),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = viewModel::register,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(RegisterScreenLocalizables.registerButtonTitle())
        }
    }
}