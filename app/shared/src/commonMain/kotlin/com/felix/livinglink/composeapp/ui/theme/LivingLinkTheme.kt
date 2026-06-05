package com.felix.livinglink.composeapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.tweener.czan.theme.CzanTheme

private val LightColors = lightColorScheme()
private val DarkColors = darkColorScheme()

@Composable
fun LivingLinkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CzanTheme(
        darkTheme = darkTheme,
        lightColorScheme = LightColors,
        darkColorScheme = DarkColors,
        content = content,
    )
}