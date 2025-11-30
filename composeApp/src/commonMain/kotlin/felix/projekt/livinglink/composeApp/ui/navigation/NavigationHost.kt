package felix.projekt.livinglink.composeApp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import felix.projekt.livinglink.composeApp.auth.domain.AuthSession
import felix.projekt.livinglink.composeApp.auth.domain.AuthTokenManager
import org.koin.compose.koinInject

@Composable
fun NavigationHost(navController: NavHostController) {
    val authTokenManager: AuthTokenManager = koinInject()
    val session by authTokenManager.session.collectAsState()
    var previous by remember { mutableStateOf<AuthSession?>(null) }

    LaunchedEffect(session) {
        if (previous is AuthSession.LoggedIn && session is AuthSession.LoggedOut) {
            val startDestinationId = navController.graph.startDestinationId
            navController.popBackStack(startDestinationId, inclusive = false)
        }
        previous = session
    }

    when (session) {
        is AuthSession.LoggedIn -> {
            LoggedInNavHost(navController)
        }

        is AuthSession.LoggedOut -> {
            LoggedOutScreen()
        }
    }
}