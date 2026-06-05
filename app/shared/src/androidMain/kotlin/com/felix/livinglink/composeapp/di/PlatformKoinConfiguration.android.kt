package com.felix.livinglink.composeapp.di

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.KoinConfiguration
import org.koin.dsl.koinConfiguration

@Composable
actual fun rememberPlatformKoinConfiguration(): KoinConfiguration {
    val context = LocalContext.current.applicationContext
    return koinConfiguration {
        androidContext(context)
    }
}