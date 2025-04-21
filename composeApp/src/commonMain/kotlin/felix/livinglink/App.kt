package felix.livinglink

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import felix.livinglink.ui.common.navigation.DefaultNavigator
import felix.livinglink.ui.common.navigation.NavigationHost
import felix.livinglink.ui.initI18n4k
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    val isDarkTheme = isSystemInDarkTheme()
    val colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme()

    val navHostController = rememberNavController()

    val appModule = remember {
        defaultAppModule(
            navigator = DefaultNavigator(navHostController)
        )
    }

    key(initI18n4k()) {
        MaterialTheme(colorScheme) {
            Column {
                NavigationHost(
                    navController = navHostController,
                    uiModule = appModule.uiModule
                )
            }
        }
    }
}