package felix.projekt.livinglink.composeApp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import felix.projekt.livinglink.composeApp.AppModule
import felix.projekt.livinglink.composeApp.auth.domain.AuthSession

@Composable
fun NavigationHost(navController: NavHostController) {
    val session by AppModule.authTokenManager.session.collectAsState()
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