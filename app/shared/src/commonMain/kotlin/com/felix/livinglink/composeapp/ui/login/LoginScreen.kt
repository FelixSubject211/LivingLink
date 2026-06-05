package com.felix.livinglink.composeapp.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tweener.czan.designsystem.atom.bars.CenterAlignedTopAppBar
import com.tweener.czan.designsystem.atom.button.Button
import com.tweener.czan.designsystem.atom.button.ButtonSize
import com.tweener.czan.designsystem.atom.button.ButtonStyle
import com.tweener.czan.designsystem.atom.dialog.AlertDialog
import com.tweener.czan.designsystem.atom.scaffold.Scaffold
import com.tweener.czan.designsystem.atom.textfield.TextField
import com.tweener.czan.designsystem.atom.textfield.TextFieldType
import com.tweener.czan.theme.Size
import org.jetbrains.compose.resources.painterResource
import livinglink.app.shared.generated.resources.Res
import livinglink.app.shared.generated.resources.compose_multiplatform
import livinglink.app.shared.generated.resources.login_api_key_placeholder
import livinglink.app.shared.generated.resources.login_connect_button
import livinglink.app.shared.generated.resources.login_error_confirm
import livinglink.app.shared.generated.resources.login_error_invalid_key
import livinglink.app.shared.generated.resources.login_error_network
import livinglink.app.shared.generated.resources.login_error_title
import livinglink.app.shared.generated.resources.login_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoginScreen(viewModel: LoginViewModel) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    state.value.error?.let { error ->
        val message = when(error) {
            is LoginScreenState.Error.InvalidKey -> stringResource(Res.string.login_error_invalid_key)
            is LoginScreenState.Error.NetworkError -> stringResource(Res.string.login_error_network)
        }

        AlertDialog(
            title = stringResource(Res.string.login_error_title),
            message = message,
            confirmButtonLabel = stringResource(Res.string.login_error_confirm),
            onConfirmButtonClicked = viewModel::closeError,
            onDismiss = viewModel::closeError
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = stringResource(Res.string.login_title),
                textStyle = MaterialTheme.typography.titleLarge,
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(Size.Padding.Large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.compose_multiplatform),
                contentDescription = null,
                modifier = Modifier
                    .size(180.dp)
                    .padding(bottom = Size.Padding.ExtraLarge),
            )

            TextField(
                text = state.value.apiKey,
                onValueChanged = viewModel::onApiKeyChanged,
                placeholderText = stringResource(Res.string.login_api_key_placeholder),
                type = TextFieldType.PASSWORD_HIDDEN,
                singleLine = true,
                imeAction = ImeAction.Done,
                capitalization = KeyboardCapitalization.None,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
            )

            Button(
                text = stringResource(Res.string.login_connect_button),
                style = ButtonStyle.PRIMARY,
                size = ButtonSize.BIG,
                enabled = state.value.canSubmit,
                loading = state.value.isLoading,
                onClick = viewModel::onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Size.Padding.Default),
            )
        }
    }
}