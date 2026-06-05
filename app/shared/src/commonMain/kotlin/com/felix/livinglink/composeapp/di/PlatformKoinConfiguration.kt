package com.felix.livinglink.composeapp.di

import androidx.compose.runtime.Composable
import org.koin.dsl.KoinConfiguration

@Composable
expect fun rememberPlatformKoinConfiguration(): KoinConfiguration