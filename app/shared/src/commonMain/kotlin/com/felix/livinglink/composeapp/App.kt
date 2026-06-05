package com.felix.livinglink.composeapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.felix.livinglink.composeapp.ui.login.LoginScreen
import com.felix.livinglink.composeapp.ui.login.LoginViewModel
import com.felix.livinglink.composeapp.di.LivingLinkClientModule
import com.felix.livinglink.composeapp.di.rememberPlatformKoinConfiguration
import com.felix.livinglink.composeapp.ui.home.HomeScreen
import com.felix.livinglink.composeapp.ui.home.HomeViewModel
import com.felix.livinglink.composeapp.ui.theme.LivingLinkTheme
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.dsl.includes
import org.koin.dsl.koinConfiguration
import org.koin.plugin.module.dsl.modules

@Composable
@Preview
fun App() {
    val platformConfig = rememberPlatformKoinConfiguration()

    KoinApplication(
        configuration = koinConfiguration(
            declaration = {
                includes(platformConfig)
                modules(LivingLinkClientModule::class)
            },
        ),
        content = {
            LivingLinkTheme {
                val rootViewModel = koinViewModel<RootViewModel>()
                val apiKey = rootViewModel.apiKey.collectAsStateWithLifecycle()

                if (apiKey.value == null) {
                    LoginScreen(viewModel = koinViewModel<LoginViewModel>())
                } else {
                    HomeScreen(viewModel = koinViewModel<HomeViewModel>())
                }
            }
        },
    )
}
