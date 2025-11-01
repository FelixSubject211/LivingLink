package felix.projekt.livinglink.composeApp.ui.loginRegistration.view

import LoginRegistrationLocalizables
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import felix.projekt.livinglink.composeApp.ui.core.view.LoadableText
import felix.projekt.livinglink.composeApp.ui.loginRegistration.viewmodel.LoginRegistrationAction
import felix.projekt.livinglink.composeApp.ui.loginRegistration.viewmodel.LoginRegistrationState

@Composable
fun LoginForm(
    dispatch: (LoginRegistrationAction) -> Unit,
    state: LoginRegistrationState.Login,
    focusRequester: FocusRequester
) {
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(modifier = Modifier.widthIn(max = 600.dp)) {
        Text(
            text = LoginRegistrationLocalizables.LoginTitle(),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = state.username,
            onValueChange = { dispatch(LoginRegistrationAction.LoginUsernameChanged(it.lowercase())) },
            maxLines = 1,
            label = { Text(LoginRegistrationLocalizables.LoginUsernameLabel()) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = state.password,
            onValueChange = { dispatch(LoginRegistrationAction.LoginPasswordChanged(it)) },
            visualTransformation = PasswordVisualTransformation(),
            maxLines = 1,
            label = { Text(LoginRegistrationLocalizables.LoginPasswordLabel()) },
            keyboardOptions = KeyboardOptions(imeAction = if (state.isLoginButtonEnabled()) ImeAction.Done else ImeAction.Previous),
            keyboardActions = KeyboardActions(onDone = { dispatch(LoginRegistrationAction.LoginSubmitted) }),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        AnimatedVisibility(visible = state.error != null) {
            Text(
                text = state.error?.localized().orEmpty(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { dispatch(LoginRegistrationAction.LoginSubmitted) },
            modifier = Modifier.fillMaxWidth(),
            enabled = state.isLoginButtonEnabled()
        ) {
            LoadableText(
                text = LoginRegistrationLocalizables.LoginButtonTitle(),
                isLoading = state.isLoading
            )
        }

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = { dispatch(LoginRegistrationAction.SwitchToRegistration) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(LoginRegistrationLocalizables.LoginNoAccountText())
        }
    }
}