package felix.projekt.livinglink.composeApp.ui.loginRegistration.view

import LoginRegistrationLocalizables
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
fun RegistrationForm(
    dispatch: (LoginRegistrationAction) -> Unit,
    state: LoginRegistrationState.Registration,
    focusRequester: FocusRequester
) {
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column {
        Text(
            text = LoginRegistrationLocalizables.RegisterTitle(),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = state.username,
            onValueChange = {
                dispatch(LoginRegistrationAction.RegistrationUsernameChanged(it.lowercase()))
            },
            maxLines = 1,
            label = { Text(LoginRegistrationLocalizables.RegisterUsernameLabel()) },
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
            onValueChange = {
                dispatch(LoginRegistrationAction.RegistrationPasswordChanged(it))
            },
            visualTransformation = PasswordVisualTransformation(),
            maxLines = 1,
            label = { Text(LoginRegistrationLocalizables.RegisterPasswordLabel()) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = state.confirmPassword,
            onValueChange = {
                dispatch(LoginRegistrationAction.RegistrationPasswordConfirmationChanged(it))
            },
            visualTransformation = PasswordVisualTransformation(),
            maxLines = 1,
            label = { Text(LoginRegistrationLocalizables.RegisterConfirmPasswordLabel()) },
            keyboardOptions = KeyboardOptions(imeAction = if (state.isRegisterButtonEnabled()) ImeAction.Done else ImeAction.Previous),
            keyboardActions = KeyboardActions(onDone = { dispatch(LoginRegistrationAction.RegistrationSubmitted) }),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        val validationError = state.passwordValidationError()

        val error = when {
            validationError != null -> validationError.localized()
            state.error != null -> state.error.localized()
            else -> null
        }

        AnimatedVisibility(visible = error != null) {
            Text(
                text = error.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { dispatch(LoginRegistrationAction.RegistrationSubmitted) },
            modifier = Modifier.fillMaxWidth(),
            enabled = state.isRegisterButtonEnabled()
        ) {
            LoadableText(
                text = LoginRegistrationLocalizables.RegisterButtonTitle(),
                isLoading = state.isLoading
            )
        }

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = { dispatch(LoginRegistrationAction.SwitchToLogin) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(LoginRegistrationLocalizables.RegisterAlreadyAccountText())
        }
    }
}
