package felix.projekt.livinglink.composeApp

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import de.comahe.i18n4k.config.I18n4kConfigImmutable
import de.comahe.i18n4k.i18n4kInitCldrPluralRules
import felix.projekt.livinglink.composeApp.auth.domain.AuthSession
import felix.projekt.livinglink.composeApp.ui.core.view.customDarkScheme
import felix.projekt.livinglink.composeApp.ui.loginRegistration.view.LoginRegistrationScreen

@Composable
fun App() {
    val i18n4kConfig = mutableStateOf(I18n4kConfigImmutable())
    i18n4kInitCldrPluralRules()

    key(i18n4kConfig) {
        val isDarkTheme = isSystemInDarkTheme()
        val colorScheme = if (isDarkTheme) customDarkScheme else lightColorScheme()

        MaterialTheme(colorScheme) {
            val session = AppModule.authTokenManager.session.collectAsState()

            when (session.value) {
                is AuthSession.LoggedIn -> {
                    MainTabBar()
                }

                AuthSession.LoggedOut -> {
                    LoginRegistrationScreen(AppModule.loginRegistrationViewModel)
                }
            }
        }
    }
}