package felix.livinglink.ui.common

import CommonLocalizables
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import felix.livinglink.common.model.LivingLinkError
import felix.livinglink.common.network.NetworkError
import felix.livinglink.ui.common.navigation.LivingLinkScreen
import felix.livinglink.ui.common.navigation.Navigator
import felix.livinglink.ui.common.state.LoadableViewModelState
import felix.livinglink.ui.common.state.ViewModelState

@Composable
fun LivingLinkError.toAlert(
    navigator: Navigator,
    onDismissRequest: () -> Unit
) {
    val baseError = when (this) {
        is ViewModelState.CombinedError<*, *> -> this.value
        is LoadableViewModelState.CombinedError<*, *, *> -> this.value
        else -> this
    }

    Alert(navigator = navigator, error = baseError, onDismissRequest = onDismissRequest)
}

@Composable
private fun Alert(
    navigator: Navigator,
    error: LivingLinkError,
    onDismissRequest: () -> Unit
) {
    val title: @Composable () -> Unit = {
        Text(
            text = error.title(),
            style = MaterialTheme.typography.bodyLarge
        )
    }

    val confirmButton: @Composable () -> Unit = if (error is NetworkError.Unauthorized) {
        {
            TextButton(onClick = {
                onDismissRequest()
                navigator.push(LivingLinkScreen.Login)
            }) {
                Text(CommonLocalizables.navigateToLoginButtonTitle())
            }
        }
    } else {
        {
            TextButton(onClick = onDismissRequest) {
                Text(CommonLocalizables.ok())
            }
        }
    }

    val textContent: (@Composable () -> Unit)? = error.message()?.let { message ->
        {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        title = title,
        text = textContent
    )
}