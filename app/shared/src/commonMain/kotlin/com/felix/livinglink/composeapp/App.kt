package com.felix.livinglink.composeapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.felix.livinglink.composeapp.di.LivingLinkClientModule
import com.felix.livinglink.composeapp.di.rememberPlatformKoinConfiguration
import com.felix.livinglink.composeapp.ui.navigation.AppNavHost
import com.felix.livinglink.composeapp.ui.theme.LivingLinkTheme
import org.koin.compose.KoinApplication
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
                AppNavHost()
            }
        },
    )
}