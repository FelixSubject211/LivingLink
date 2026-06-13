package com.felix.livinglink.composeapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.felix.livinglink.composeapp.RootViewModel
import com.felix.livinglink.composeapp.auth.domain.AuthState
import com.felix.livinglink.composeapp.ui.login.LoginScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppNavHost(
    rootViewModel: RootViewModel = koinViewModel(),
) {
    val authState by rootViewModel.authState.collectAsStateWithLifecycle()
    val navController = rememberNavController()

    // Initialwert wird synchron aus den lokalen Credentials geseedet -> kein Login-Flash.
    val startDestination = remember { authState.toDestination() }

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable<Destination.Login> {
            LoginScreen(viewModel = koinViewModel())
        }
        composable<Destination.Main> {
            MainScreen()
        }
    }

    LaunchedEffect(authState) {
        val target = authState.toDestination()
        val alreadyThere = navController.currentDestination
            ?.hierarchy
            ?.any { it.hasRoute(target::class) } == true

        if (!alreadyThere) {
            navController.navigate(target) {
                popUpTo(navController.graph.findStartDestination().id) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }
}

private fun AuthState.toDestination(): Destination =
    when (this) {
        is AuthState.LoggedIn -> Destination.Main
        is AuthState.LoggedOut -> Destination.Login
    }