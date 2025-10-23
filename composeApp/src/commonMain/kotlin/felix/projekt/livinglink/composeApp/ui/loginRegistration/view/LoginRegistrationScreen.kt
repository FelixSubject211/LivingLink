package felix.projekt.livinglink.composeApp.ui.loginRegistration.view

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import felix.projekt.livinglink.composeApp.ui.core.view.AnimatedSwitch
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.ViewModel
import felix.projekt.livinglink.composeApp.ui.loginRegistration.viewmodel.LoginRegistrationAction
import felix.projekt.livinglink.composeApp.ui.loginRegistration.viewmodel.LoginRegistrationState
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class, ExperimentalAnimationApi::class)
@Composable
fun LoginRegistrationScreen(
    viewModel: ViewModel<LoginRegistrationState, LoginRegistrationAction, Nothing>
) {
    val state by viewModel.state.collectAsState()
    val focusRequester = remember { FocusRequester() }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        LazyColumn(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            item {
                AnimatedSwitch(
                    targetState = state::class,
                    isForward = state is LoginRegistrationState.Registration
                ) {
                    when (state) {
                        is LoginRegistrationState.Login -> LoginForm(
                            dispatch = viewModel::dispatch,
                            state = state as LoginRegistrationState.Login,
                            focusRequester = focusRequester
                        )

                        is LoginRegistrationState.Registration -> RegistrationForm(
                            dispatch = viewModel::dispatch,
                            state = state as LoginRegistrationState.Registration,
                            focusRequester = focusRequester
                        )
                    }
                }
            }
        }
    }
}